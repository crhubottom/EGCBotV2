package com.egc.bot.audio;

import com.egc.bot.database.gameDB;
import com.egc.bot.events.rocketEvent;
import com.egc.bot.events.tipEvent;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.egc.bot.Bot.*;

public class commandListener {
    public static final int SILENCE_THRESHOLD = 200;
    public static final int MIN_SPEECH_MS = 500;
    public static final int SPEECH_TIMEOUT_MS = 500;
    public static final int MAX_SPEECH_MS = 5000;

    // Removed very short / overly broad triggers like "egc", "gc", "ec"
    private static final String[] ACTIVATION_KEYWORD = new String[]{
            "hey egc bot",
            "egc bot",
            "egcbot",
            "e g c bot",
            "hey computer",
            "hey bot",
            "gcbot",
            "gc bot",
            "etc bot",

            // common phonetic shifts
            "easy bot",
            "easy but",
            "easy bought",
            "ec bot",
            "ec but",
            "ec bought",
            "e c bot",
            "e c but",
            "e c bought",
            "g c bot",
            "g c but",
            "g c bought",

            // consonant confusion
            "ejc bot",
            "edc bot",
            "ebc bot",
            "dgc bot",
            "gec bot",

            // reordered / partial triggers
            "bot egc",
            "bot gc",
            "bot ec",

            // “bot” misheard variants
            "egc box",
            "egc boxx",
            "egc back",
            "egc bad",
            "gc box",
            "gc back",
            "gc bad",

            // weird but real speech-to-text guesses
            "egypt bot",
            "agency bot",
            "edge bot",
            "eat you bot",
            "eat see bot",
            "each bot",

            // “hey” variations
            "hey egc",
            "hey gc",
            "hey ec",
            "hey easy bot",
            "hey gc bot",

            // other assistant-style wake phrases
            "ok egc",
            "ok gc",
            "ok bot",
            "yo egc",
            "yo bot"
    };

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

    private void processUserAudio(Long userId, byte[] audioData) {
        executorService.submit(() -> {
            try {
                User user = client.getUserById(userId);
                Member member = client.getGuildById(guildID) != null
                        ? client.getGuildById(guildID).getMemberById(userId)
                        : null;

                String username;
                if (member != null && member.getNickname() != null && !member.getNickname().isBlank()) {
                    username = member.getNickname();
                } else if (user != null) {
                    username = user.getName();
                } else {
                    username = "Unknown User";
                }

                File wavFile = convertToWav(audioData);
                String transcription = transcribeAudio(wavFile);

                if (transcription != null) {
                    String lower = transcription.toLowerCase().trim();

                    if (!lower.equals("bye.")
                            && !lower.equals("thank you.")
                            && !lower.equals("thanks.")
                            && !lower.isBlank()) {

                        String matchedKeyword = findBestActivationKeyword(lower);

                        if (matchedKeyword != null) {
                            String command = lower.substring(
                                    lower.indexOf(matchedKeyword) + matchedKeyword.length()
                            ).trim();

                            if (!command.isEmpty()) {
                                PlayerManager.get().play(
                                        client.getGuildById(guildID),
                                        "ytsearch:Apple Pay Success Sound Effect"
                                );

                                processCommand(userId, username, command);
                            }
                        }
                    }
                }

                wavFile.delete();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                processingUsers.put(userId, false);
            }
        });
    }

    private String findBestActivationKeyword(String text) {
        String bestMatch = null;

        for (String keyword : ACTIVATION_KEYWORD) {
            if (containsWakeWord(text, keyword)) {
                if (bestMatch == null || keyword.length() > bestMatch.length()) {
                    bestMatch = keyword;
                }
            }
        }

        return bestMatch;
    }

    private boolean containsWakeWord(String text, String wakeWord) {
        String pattern = "\\b" + Pattern.quote(wakeWord) + "\\b";
        return Pattern.compile(pattern).matcher(text).find();
    }

    private File convertToWav(byte[] pcmData) throws Exception {
        long startTime = System.nanoTime();

        AudioFormat format = new AudioFormat(48000, 16, 2, true, true);
        File outputFile = File.createTempFile("discord-audio", ".wav");

        try (AudioInputStream pcmStream = new AudioInputStream(
                new ByteArrayInputStream(pcmData),
                format,
                pcmData.length / format.getFrameSize()
        );
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            AudioSystem.write(pcmStream, AudioFileFormat.Type.WAVE, outputFile);
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;
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
                "You are transcribing voice audio. Your name is E-G-C Bot, a friendly discord bot. This was said by the user "
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

        System.out.println(out);

        if (out.startsWith("play ")) {
            String name = out.substring(5);
            out = "Playing " + name;

            try {
                new URL(name);
            } catch (MalformedURLException e) {
                name = "ytsearch:" + name;
            }

            PlayerManager.get().play(client.getGuildById(guildID), name);
            audio = false;

        } else if (out.startsWith("rocket_LSP ")) {
            String lsp = out.substring(11);
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

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;
        System.out.println("processing command took " + duration + "ms");
    }
}