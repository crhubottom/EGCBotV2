package com.egc.bot.audio;

import com.egc.bot.database.gameDB;
import com.egc.bot.events.rocketEvent;
import com.egc.bot.events.tipEvent;
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
import java.util.concurrent.ConcurrentHashMap;

import static com.egc.bot.Bot.*;

public class commandListener {
    public static final int SILENCE_THRESHOLD = 200; // Adjust based on testing
    public static final int MIN_SPEECH_MS = 500; // Minimum length to consider as speech (ms)
    public static final int SPEECH_TIMEOUT_MS = 500; // Silence duration to consider speech complete (ms)
    public static final int MAX_SPEECH_MS = 5000; // Maximum speech length to prevent runaway recording (ms)
    private static final String[] ACTIVATION_KEYWORD = new String[]{"egc bot","egcbot","egc but","egc bought","hey computer","hey bot", "e g c bot"};
    public static final Map<Long, Boolean> processingUsers = new ConcurrentHashMap<>();


    public void startAudioProcessing() {
        executorService.submit(() -> {
            while (true) {
                try {
                    // Check for completed speech segments from each user
                    Map<Long, byte[]> completedSpeech = receiverHandler.getCompletedSpeechSegments();

                    // Process each completed speech segment
                    for (Map.Entry<Long, byte[]> entry : completedSpeech.entrySet()) {
                        Long userId = entry.getKey();
                        byte[] audioData = entry.getValue();

                        // Skip if already processing for this user
                        if (processingUsers.getOrDefault(userId, false)) {
                            continue;
                        }

                        // Process this user's audio
                        if (audioData.length > 0) {
                            processingUsers.put(userId, true);
                            processUserAudio(userId, audioData);
                        }
                    }

                    Thread.sleep(100); // Short sleep to avoid CPU hogging
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void processUserAudio(Long userId, byte[] audioData) {
        executorService.submit(() -> {
            try {
                // Get username for logging
                User user = client.getUserById(userId);
                String username = user != null ? client.getGuildById(guildID).getMemberById(userId).getNickname() : "Unknown User";
                // Convert audio data to WAV file
                File wavFile = convertToWav(audioData);

                // Transcribe audio using Whisper API
                String transcription = transcribeAudio(wavFile);
                if(!transcription.toLowerCase().equals("bye.")&&!transcription.toLowerCase().equals("thank you.")&&!transcription.toLowerCase().equals("thanks.")&&!transcription.toLowerCase().equals("")&&!transcription.isBlank()) {
                    // Check for activation keyword
                    for (String s : ACTIVATION_KEYWORD) {
                        if (transcription.toLowerCase().contains(s)) {
                            // Extract command (everything after the keyword)

                            PlayerManager playerManager = PlayerManager.get();
                            playerManager.play(client.getGuildById(guildID), "ytsearch:Apple Pay Success Sound Effect");
                            String command = transcription.toLowerCase()
                                    .substring(transcription.toLowerCase().indexOf(s)
                                            + s.length())
                                    .trim();

                            // Process the command for this specific user
                            processCommand(userId, username, command);
                        }
                    }
                }

                // Clean up temporary files
                wavFile.delete();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Mark this user as no longer being processed
                processingUsers.put(userId, false);
            }
        });

    }

    private File convertToWav(byte[] pcmData) throws Exception {
        long startTime = System.nanoTime();
        // PCM audio format settings
        AudioFormat format = new AudioFormat(48000, 16, 2, true, true);

        // Create temporary WAV file
        File outputFile = File.createTempFile("discord-audio", ".wav");

        try (AudioInputStream pcmStream = new AudioInputStream(
                new ByteArrayInputStream(pcmData), format, pcmData.length / format.getFrameSize());
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            // Write WAV header
            AudioSystem.write(pcmStream, AudioFileFormat.Type.WAVE, outputFile);
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;
        System.out.println("converting took " + duration + " ms");
        return outputFile;
    }

    private String transcribeAudio(File audioFile){
        System.out.println("transcribeAudio");
        return(AIc.deepgramSpeechToText(audioFile));
    }

    private void processCommand(Long userId, String username, String command) throws SQLException, IOException, InterruptedException {
        boolean audio=true;
        long startTime = System.nanoTime();
        System.out.println("Processing command: " + command);
        String out = AIc.gptCallWithSystem(command,"You are transcribing voice audio. Your name is E-G-C Bot, a friendly discord bot. This was said by the user"+username+". " +
                "Say \"play \"+song_name if the user is requesting a song to be played. " +
                "Say \"skip\" if the user is requesting to skip the song." +
                "Say \"tip\" if the user is requesting a game tip. " +
                "Say \"spacex\" if the user is asking what the next SpaceX launch is." +
                "Say \"rocket\" if the user is asking what the next rocket launch (in general) is." +
                "Say \"rocket_LSP \"+LSP if the user is asking what the next rocket launch from an LSP that is not SpaceX. Do not give the LSP in acronyms, use the full name." +
                "Say \"major_order\" if the user is asking what the Helldivers major order is." +
                "Say \"top_gold\" if the user is asking who has the most gold." +
                "Say \"my_gold\" if the user is asking how much gold they have." +
                "Say \"top_game\" if the user is asking what the top played game is." +
                "If the question is cut off or does not make sense, do not respond"+
                "Respond normally for anything else.","gpt-4o-mini");
        System.out.println(out);
        if(out.startsWith("play ")){
            String name=out.substring(4);
            out="Playing "+name;
            try {
                new URL(name);
            } catch (MalformedURLException e) {
                name = "ytsearch:" + name;
            }
            PlayerManager playerManager = PlayerManager.get();
            playerManager.play(client.getGuildById(guildID), name);
            audio=false;

        }else if(out.startsWith("rocket_LSP ")){
            String lsp=out.substring(11);
            System.out.println(lsp);
            out= rocketEvent.nextLaunchWithLSP(lsp).toString();
        }
        switch(out){
            case "skip":
                audio=false;
                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(client.getGuildById(guildID));
                guildMusicManager.getTrackScheduler().getPlayer().stopTrack();
                break;
            case "tip":
                tipEvent tipE=new tipEvent();
                out=tipE.tip();
                audio=false;
                break;
            case "spacex":
                out= rocketEvent.nextLaunch(false,true).toString();
                break;
            case "rocket":
                out= rocketEvent.nextLaunch(true,true).toString();
                break;
            case "major_order":
                String orderDesc=null;
                JSONArray jsonArray  = new JSONArray(IOUtils.toString(new URL("https://helldiverstrainingmanual.com/api/v1/war/major-orders"), StandardCharsets.UTF_8));
                String j=jsonArray.toString();
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject rec = jsonArray.getJSONObject(i);
                    JSONObject setting = rec.getJSONObject("setting");
                    orderDesc = setting.getString("overrideBrief");
                }
                out="The current HellDivers Major Order is "+orderDesc;
                break;
            case "top_gold":
                out="The user with the most gold is "+inv.topGold();
                break;
            case "my_gold":
                out="You have "+inv.getGold(userId)+" gold.";
                break;
            case "top_game":
                out="The most played game is "+ gameDB.topGame();
                break;
            default:
                break;

        }
        if(audio) {
            try {
                AIc.ttsCall(out, "output");
                PlayerManager playerManager = PlayerManager.get();
                playerManager.play(client.getGuildById(guildID), "output.mp3");

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000 ;
        System.out.println("processing command took "+duration+"ms");
    }
}
