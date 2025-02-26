package com.egc.bot.audio;

import net.dv8tion.jda.api.audio.CombinedAudio;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.egc.bot.Bot.*;
import static com.egc.bot.audio.commandListener.*;


public class AudioReceiveHandler implements net.dv8tion.jda.api.audio.AudioReceiveHandler {
    // Map of user IDs to their voice state
    private final Map<Long, UserVoiceState> userVoiceStates = new ConcurrentHashMap<>();
    // Map for completed speech segments ready for processing
    private final Map<Long, byte[]> completedSpeechSegments = new ConcurrentHashMap<>();

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public void handleUserAudio(net.dv8tion.jda.api.audio.UserAudio userAudio) {
        long userId = userAudio.getUser().getIdLong();
        byte[] audioData = userAudio.getAudioData(1.0f);

        // Get or create voice state for this user
        UserVoiceState voiceState = userVoiceStates.computeIfAbsent(userId,
                id -> new UserVoiceState());

        // Update voice state with new audio data
        synchronized (voiceState) {
            voiceState.processAudioChunk(audioData);

            // If speech is completed, add to completed segments
            if (voiceState.isSpeechComplete()) {
                byte[] completedSpeech = voiceState.getAndClearSpeechBuffer();
                if (completedSpeech.length > 0) {
                    completedSpeechSegments.put(userId, completedSpeech);
                }
            }
        }
    }

    // Get completed speech segments and clear the map
    public Map<Long, byte[]> getCompletedSpeechSegments() {
        Map<Long, byte[]> result = new ConcurrentHashMap<>(completedSpeechSegments);
        completedSpeechSegments.clear();
        return result;
    }

    // Class to track speech state for a single user
    private static class UserVoiceState {
        private final ByteArrayOutputStream speechBuffer = new ByteArrayOutputStream();
        private boolean isSpeaking = false;
        private long speechStartTime = 0;
        private long lastSpeechTime = 0;

        // Process a chunk of audio and determine if it contains speech
        public void processAudioChunk(byte[] audioData) {
            boolean containsSpeech = detectSpeech(audioData);
            long currentTime = System.currentTimeMillis();

            if (containsSpeech) {
                // If we weren't speaking before, mark the start of speech
                if (!isSpeaking) {
                    isSpeaking = true;
                    speechStartTime = currentTime;
                }

                // Update the last time we detected speech
                lastSpeechTime = currentTime;

                // Add the audio data to the buffer
                speechBuffer.write(audioData, 0, audioData.length);
            } else if (isSpeaking) {
                // We're in speech mode but detected silence
                // Still add the data to capture pauses naturally
                speechBuffer.write(audioData, 0, audioData.length);
            }
        }

        // Check if the current speech segment is complete
        public boolean isSpeechComplete() {
            if (!isSpeaking) {
                return false;
            }

            long currentTime = System.currentTimeMillis();

            // Speech is complete if:
            // 1. We've been silent for SPEECH_TIMEOUT_MS milliseconds
            boolean silenceTimeout = currentTime - lastSpeechTime > SPEECH_TIMEOUT_MS;

            // 2. OR Speech has gone on too long (prevent endless recording)
            boolean maxDurationReached = currentTime - speechStartTime > MAX_SPEECH_MS;

            // 3. AND Speech is at least MIN_SPEECH_MS milliseconds long
            boolean minDurationReached = speechBuffer.size() > 0 &&
                    (currentTime - speechStartTime) > MIN_SPEECH_MS;

            if ((silenceTimeout || maxDurationReached) && minDurationReached) {
                isSpeaking = false;
                return true;
            }

            return false;
        }

        // Get speech buffer and reset for next speech segment
        public byte[] getAndClearSpeechBuffer() {
            byte[] data = speechBuffer.toByteArray();
            speechBuffer.reset();
            return data;
        }

        // Simple voice activity detection based on audio energy
        private boolean detectSpeech(byte[] audioData) {
            if (audioData.length < 2) {
                return false;
            }

            // Convert to short samples
            ByteBuffer byteBuffer = ByteBuffer.wrap(audioData);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);

            int sampleCount = audioData.length / 2; // 16-bit samples
            long energySum = 0;

            for (int i = 0; i < sampleCount; i++) {
                short sample = byteBuffer.getShort();
                energySum += Math.abs(sample);
            }

            // Calculate average energy
            double averageEnergy = energySum / (double) sampleCount;

            // Return true if energy is above threshold
            return averageEnergy > SILENCE_THRESHOLD;
        }
    }

}
