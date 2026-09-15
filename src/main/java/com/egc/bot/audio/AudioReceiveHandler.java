package com.egc.bot.audio;

import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.egc.bot.audio.commandListener.MAX_SPEECH_MS;
import static com.egc.bot.audio.commandListener.SILENCE_THRESHOLD;

/**
 * Receives Discord voice audio and uses an offline Vosk recognizer to detect a wake
 * phrase locally. Only the audio that follows the wake phrase is buffered and handed
 * off for cloud transcription.
 *
 * Flow per user:
 *   LISTENING -> feed downsampled audio to Vosk, discard it, watch partial results
 *   (wake phrase matched)
 *   ARMED     -> buffer the raw 48kHz audio until silence or the max-duration cap
 *   -> publish to completedSpeechSegments, return to LISTENING
 */
public class AudioReceiveHandler implements net.dv8tion.jda.api.audio.AudioReceiveHandler {

    // ---- Configuration -----------------------------------------------------

    /** Directory (not a file) containing the unzipped Vosk model. */
    private static final String MODEL_PATH = "models/vosk-model-small-en-us-0.15";

    /**
     * Restricted grammar. Vosk will only ever emit these phrases or [unk], which makes
     * matching reliable and much cheaper than open-vocabulary recognition.
     * "EGC" is out of vocabulary, so it is spelled as individual letters.
     */
    private static final String GRAMMAR =
            "[\"e g c bot\", \"hey e g c bot\", \"hey bot\", \"[unk]\"]";

    private static final String[] WAKE_PHRASES = {
            "e g c bot",
            "hey e g c bot",
            "hey bot"

    };

    /** Silence after the wake phrase before the command is considered finished. */
    private static final int COMMAND_TIMEOUT_MS = 1200;

    /**
     * How much audio preceding the wake match to prepend to the command buffer.
     * Vosk's partial results lag the audio slightly; without this the first word of
     * the command can be clipped. The tradeoff is that the tail of the wake phrase
     * usually ends up in the transcription, which the model prompt ignores.
     */
    private static final int PREROLL_MS = 300;

    /** Discard captures this short; they contain no command. */
    private static final int MIN_COMMAND_MS = 250;

    private static final int DISCORD_CHUNK_MS = 20;
    private static final int PREROLL_CHUNKS = PREROLL_MS / DISCORD_CHUNK_MS;

    /** 48kHz stereo 16-bit is 192 bytes per millisecond. */
    private static final int BYTES_PER_MS_48K_STEREO = 192;

    // ---- Shared state ------------------------------------------------------

    /** The Model is thread-safe and shared; Recognizers are per-user and are not. */
    private static final Model VOSK_MODEL;

    static {
        LibVosk.setLogLevel(LogLevel.WARNINGS);
        try {
            VOSK_MODEL = new Model(MODEL_PATH);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not load the Vosk model from '" + MODEL_PATH + "'. Download it from "
                            + "https://alphacephei.com/vosk/models and unzip it to that path.", e);
        }
    }

    private static final Map<Long, UserVoiceState> userVoiceStates = new ConcurrentHashMap<>();
    private static final Map<Long, byte[]> completedSpeechSegments = new ConcurrentHashMap<>();

    // ---- JDA callbacks -----------------------------------------------------

    @Override
    public boolean canReceiveUser() {
        return true;
    }
    /** Release every per-user recognizer but keep the model loaded. */
    public static void clearUsers() {
        for (Long userId : userVoiceStates.keySet()) {
            removeUser(userId);
        }
    }
    @Override
    public void handleUserAudio(net.dv8tion.jda.api.audio.UserAudio userAudio) {
        long userId = userAudio.getUser().getIdLong();
        byte[] audioData = userAudio.getAudioData(1.0f);

        UserVoiceState voiceState = userVoiceStates.computeIfAbsent(userId, id -> new UserVoiceState());

        synchronized (voiceState) {
            voiceState.processAudioChunk(audioData);

            if (voiceState.isSpeechComplete()) {
                byte[] completedSpeech = voiceState.getAndClearSpeechBuffer();
                if (completedSpeech.length > 0) {
                    completedSpeechSegments.put(userId, completedSpeech);
                }
            }
        }
    }

    // ---- Public API --------------------------------------------------------

    /** Drain and clear the finished command segments. */
    public Map<Long, byte[]> getCompletedSpeechSegments() {
        Map<Long, byte[]> result = new ConcurrentHashMap<>(completedSpeechSegments);
        completedSpeechSegments.clear();
        return result;
    }

    /**
     * Discord stops delivering packets when a user goes silent, so the timeout that
     * ends a command has to be checked on a timer as well as on each packet.
     */
    public void pollCompletedSpeechSegments() {
        for (Map.Entry<Long, UserVoiceState> entry : userVoiceStates.entrySet()) {
            Long userId = entry.getKey();
            UserVoiceState voiceState = entry.getValue();

            synchronized (voiceState) {
                if (voiceState.isSpeechComplete()) {
                    byte[] completedSpeech = voiceState.getAndClearSpeechBuffer();
                    if (completedSpeech.length > 0) {
                        completedSpeechSegments.put(userId, completedSpeech);
                    }
                }
            }
        }
    }

    /**
     * Release the native recognizer for a user. Call this from a
     * GuildVoiceUpdateEvent listener when someone leaves the channel, otherwise
     * native memory accumulates over a long uptime.
     */
    public static void removeUser(long userId) {
        UserVoiceState state = userVoiceStates.remove(userId);
        if (state != null) {
            synchronized (state) {
                state.close();
            }
        }
        completedSpeechSegments.remove(userId);
    }

    /** Release every recognizer and the shared model. Call on bot shutdown. */
    public static void shutdown() {
        clearUsers();
        VOSK_MODEL.close();
    }

    // ---- Audio conversion --------------------------------------------------

    /**
     * Discord delivers 48kHz, stereo, 16-bit big-endian PCM.
     * Vosk wants 16kHz, mono, 16-bit little-endian.
     *
     * A 20ms Discord chunk is 960 frames per channel and 960 / 3 = 320, so chunks
     * convert cleanly with nothing left over. Groups of three samples are averaged
     * rather than dropped; naive decimation aliases and measurably hurts accuracy.
     */
    static byte[] toVoskPcm(byte[] discordPcm) {
        int frames = discordPcm.length / 4;      // 4 bytes per stereo frame
        int outSamples = frames / 3;             // 48kHz -> 16kHz
        if (outSamples == 0) {
            return new byte[0];
        }

        byte[] out = new byte[outSamples * 2];
        ByteBuffer in = ByteBuffer.wrap(discordPcm).order(ByteOrder.BIG_ENDIAN);
        ByteBuffer ob = ByteBuffer.wrap(out).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < outSamples; i++) {
            int acc = 0;
            for (int j = 0; j < 3; j++) {
                int left = in.getShort();
                int right = in.getShort();
                acc += (left + right) / 2;       // downmix to mono
            }
            ob.putShort((short) (acc / 3));      // crude anti-alias + decimate
        }
        return out;
    }

    // ---- Per-user state ----------------------------------------------------

    private static class UserVoiceState {

        private final Recognizer recognizer;

        /** Raw 48kHz chunks held back so the command's first word isn't clipped. */
        private final Deque<byte[]> preRoll = new ArrayDeque<>(PREROLL_CHUNKS + 1);

        /** Raw 48kHz audio of the command, in the format convertToWav() expects. */
        private final ByteArrayOutputStream commandBuffer = new ByteArrayOutputStream();

        private boolean armed = false;
        private long armedAt = 0;
        private long lastSpeechTime = 0;
        private boolean closed = false;

        UserVoiceState() {
            this.recognizer = new Recognizer(VOSK_MODEL, 16000f, GRAMMAR);
        }

        void processAudioChunk(byte[] audioData) {
            if (closed || audioData == null || audioData.length == 0) {
                return;
            }
            long now = System.currentTimeMillis();

            if (!armed) {
                listenForWakePhrase(audioData, now);
            } else {
                commandBuffer.write(audioData, 0, audioData.length);
                if (detectSpeech(audioData)) {
                    lastSpeechTime = now;
                }
            }
        }

        private void listenForWakePhrase(byte[] audioData, long now) {
            // Keep a short rolling window of raw audio for the pre-roll.
            preRoll.addLast(audioData);
            while (preRoll.size() > PREROLL_CHUNKS) {
                preRoll.removeFirst();
            }

            byte[] pcm = toVoskPcm(audioData);
            if (pcm.length == 0) {
                return;
            }

            // A true return means Vosk decided an utterance ended. The final result is
            // not useful here, so reset and keep going.
            if (recognizer.acceptWaveForm(pcm, pcm.length)) {
                recognizer.reset();
                return;
            }

            // Partial results are checked rather than final ones: waiting for Vosk's
            // endpointing would add roughly half a second before the bot reacts.
            String partial = new JSONObject(recognizer.getPartialResult()).optString("partial", "");
            if (partial.isEmpty() || !matchesWake(partial)) {
                return;
            }

            recognizer.reset();
            armed = true;
            armedAt = now;
            lastSpeechTime = now;

            commandBuffer.reset();
            for (byte[] chunk : preRoll) {
                commandBuffer.write(chunk, 0, chunk.length);
            }
            preRoll.clear();

            onWakeDetected();
        }

        /**
         * Hook for immediate user feedback. Firing the acknowledgement sound here
         * rather than after transcription is most of the perceived latency win.
         */
        private void onWakeDetected() {
            System.out.println("Wake phrase detected");
            // PlayerManager.get().play(client.getGuildById(guildID),
            //         "ytsearch:Apple Pay Success Sound Effect");
        }

        private boolean matchesWake(String partial) {
            for (String phrase : WAKE_PHRASES) {
                if (partial.contains(phrase)) {
                    return true;
                }
            }
            return false;
        }

        boolean isSpeechComplete() {
            if (!armed) {
                return false;
            }
            long now = System.currentTimeMillis();

            boolean silenceTimeout = now - lastSpeechTime > COMMAND_TIMEOUT_MS;
            boolean maxDurationReached = now - armedAt > MAX_SPEECH_MS;

            if (silenceTimeout || maxDurationReached) {
                armed = false;
                return true;
            }
            return false;
        }

        byte[] getAndClearSpeechBuffer() {
            byte[] data = commandBuffer.toByteArray();
            commandBuffer.reset();

            int durationMs = data.length / BYTES_PER_MS_48K_STEREO;
            if (durationMs < MIN_COMMAND_MS) {
                System.out.println("Discarding empty capture (" + durationMs + " ms)");
                return new byte[0];
            }
            return data;
        }

        /** Energy-based voice activity detection, used only to time out the command. */
        private boolean detectSpeech(byte[] audioData) {
            if (audioData.length < 2) {
                return false;
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(audioData).order(ByteOrder.BIG_ENDIAN);
            int sampleCount = audioData.length / 2;   // 16-bit samples
            long energySum = 0;

            for (int i = 0; i < sampleCount; i++) {
                energySum += Math.abs(byteBuffer.getShort());
            }

            double averageEnergy = energySum / (double) sampleCount;
            return averageEnergy > SILENCE_THRESHOLD;
        }

        void close() {
            if (!closed) {
                closed = true;
                recognizer.close();
            }
        }
    }
}