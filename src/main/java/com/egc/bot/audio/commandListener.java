package com.egc.bot.audio;

import com.egc.bot.database.gameDB;
import com.egc.bot.events.rocketEvent;
import com.egc.bot.events.tipEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.egc.bot.Bot.*;

public class commandListener {

    /** Energy threshold used by the VAD in AudioReceiveHandler. */
    public static final int SILENCE_THRESHOLD = 200;

    /** Hard cap on how long a single command may run before it is cut off. */
    public static final int MAX_SPEECH_MS = 5000;

    /**
     * Deepgram tends to emit these on near-silent or noise-only audio. The wake word
     * is now matched offline, so anything reaching here should be a real command, but
     * a short burst of background noise can still slip through.
     */
    private static final Set<String> FILLER_TRANSCRIPTS =
            Set.of("bye.", "bye", "thank you.", "thank you", "thanks.", "thanks", "you", "uh", ".");

    public static final Map<Long, Boolean> processingUsers = new ConcurrentHashMap<>();

    public void startAudioProcessing() {
        executorService.submit(() -> {
            while (true) {
                try {
                    receiverHandler.pollCompletedSpeechSegments();
                    Map<Long, byte[]> completedSpeech = receiverHandler.getCompletedSpeechSegments();

                    for (Map.Entry<Long, byte[]> entry : completedSpeech.entrySet()) {
                        Long userId = entry.getKey();
                        byte[] audioData = entry.getValue();

                        if (processingUsers.getOrDefault(userId, false)) {
                            continue;
                        }

                        if (audioData.length > 0) {
                            processingUsers.put(userId, true);
                            processUserAudio(userId, audioData);
                        }
                    }

                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * The wake phrase has already been matched offline by AudioReceiveHandler, so the
     * audio handed in here is the command itself. Transcribe it and act on it.
     */
    private void processUserAudio(Long userId, byte[] audioData) {
        executorService.submit(() -> {
            File wavFile = null;
            try {
                String username = resolveUsername(userId);
                wavFile = convertToWav(audioData);

                String transcription = transcribeAudio(wavFile);
                if (transcription == null) {
                    return;
                }

                String command = transcription.trim();
                if (command.isBlank() || FILLER_TRANSCRIPTS.contains(command.toLowerCase())) {
                    System.out.println("Ignoring empty or filler transcription: '" + command + "'");
                    return;
                }

                processCommand(userId, username, command);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (wavFile != null && !wavFile.delete()) {
                    wavFile.deleteOnExit();
                }
                processingUsers.put(userId, false);
            }
        });
    }

    /** Server nickname if there is one, otherwise the account name. */
    private String resolveUsername(Long userId) {
        try {
            Guild guild = client.getGuildById(guildID);
            Member member = guild != null ? guild.getMemberById(userId) : null;
            if (member != null) {
                return member.getEffectiveName();
            }

            User user = client.getUserById(userId);
            if (user != null) {
                return user.getName();
            }
        } catch (Exception e) {
            // fall through
        }
        return "Unknown User";
    }

    private File convertToWav(byte[] pcmData) throws Exception {
        long startTime = System.nanoTime();

        AudioFormat format = new AudioFormat(48000, 16, 2, true, true);
        File outputFile = File.createTempFile("discord-audio", ".wav");

        try (AudioInputStream pcmStream = new AudioInputStream(
                new ByteArrayInputStream(pcmData),
                format,
                pcmData.length / format.getFrameSize()
        )) {
            AudioSystem.write(pcmStream, AudioFileFormat.Type.WAVE, outputFile);
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        System.out.println("converting took " + duration + " ms");

        return outputFile;
    }

    private String transcribeAudio(File audioFile) {
        System.out.println("transcribeAudio");
        return AIc.deepgramSpeechToText(audioFile);
    }

    private void processCommand(Long userId, String username, String command)
            throws SQLException, IOException, InterruptedException {

        boolean audio = true;
        long startTime = System.nanoTime();
        System.out.println("Processing command: " + command);

        String out = AIc.gptCallWithSystem(
                command,
                "You are transcribing voice audio. Your name is E-G-C Bot, a friendly discord bot. \"The transcription may begin with a fragment of the wake phrase; ignore it. \"This was said by the user "
                        + username + ". "
                        + "Say \"play \"+song_name if the user is requesting a song to be played. "
                        + "Say \"skip\" if the user is requesting to skip the song. "
                        + "Say \"tip\" if the user is requesting a game tip. "
                        + "Say \"spacex\" if the user is asking what the next SpaceX launch is. "
                        + "Say \"rocket\" if the user is asking what the next rocket launch (in general) is. "
                        + "Say \"rocket_LSP \"+LSP if the user is asking what the next rocket launch from an LSP that is not SpaceX. Do not give the LSP in acronyms, use the full name. "
                        + "Say \"major_order\" if the user is asking what the Helldivers major order is. "
                        + "Say \"top_gold\" if the user is asking who has the most gold. "
                        + "Say \"my_gold\" if the user is asking how much gold they have. "
                        + "Say \"top_game\" if the user is asking what the top played game is. "
                        + "If the question is cut off or does not make sense, do not respond. "
                        + "Respond to the user normally for anything else, do not just repeat what they said.",
                textModel
        );

        if (out == null || out.isBlank()) {
            System.out.println("No response from model");
            return;
        }

        out = out.trim();
        System.out.println(out);

        if (out.startsWith("play ")) {
            String name = out.substring(5).trim();
            out = "Playing " + name;

            try {
                new URL(name);
            } catch (MalformedURLException e) {
                name = "ytsearch:" + name;
            }

            PlayerManager.get().play(client.getGuildById(guildID), name);
            audio = false;

        } else if (out.startsWith("rocket_LSP ")) {
            String lsp = out.substring(11).trim();
            System.out.println(lsp);
            out = rocketEvent.nextLaunchWithLSP(lsp).toString();
        }

        switch (out) {
            case "skip":
                audio = false;
                GuildMusicManager guildMusicManager =
                        PlayerManager.get().getGuildMusicManager(client.getGuildById(guildID));
                guildMusicManager.getTrackScheduler().getPlayer().stopTrack();
                break;

            case "tip":
                tipEvent tipE = new tipEvent();
                out = tipE.tip();
                audio = false;
                break;

            case "spacex":
                out = rocketEvent.nextLaunch(false, true).toString();
                break;

            case "rocket":
                out = rocketEvent.nextLaunch(true, true).toString();
                break;

            case "major_order":
                String orderDesc = null;
                JSONArray jsonArray = new JSONArray(
                        IOUtils.toString(
                                new URL("https://helldiverstrainingmanual.com/api/v1/war/major-orders"),
                                StandardCharsets.UTF_8
                        )
                );

                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject rec = jsonArray.getJSONObject(i);
                    JSONObject setting = rec.getJSONObject("setting");
                    orderDesc = setting.getString("overrideBrief");
                }

                out = "The current HellDivers Major Order is " + orderDesc;
                break;

            case "top_gold":
                out = "The user with the most gold is " + inv.topGold();
                break;

            case "my_gold":
                out = "You have " + inv.getGold(userId) + " gold.";
                break;

            case "top_game":
                out = "The most played game is " + gameDB.topGame();
                break;

            default:
                break;
        }

        if (audio) {
            try {
                AIc.ttsCall(out, "outputvoice");
                PlayerManager.get().play(client.getGuildById(guildID), "outputvoice.mp3");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        System.out.println("processing command took " + duration + "ms");
    }
}