package com.egc.bot;

import com.egc.bot.audio.PlayerManager;
import com.egc.bot.database.messageDB;
import com.egc.bot.database.settingsDB;
import com.egc.bot.events.countTracker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.egc.bot.Bot.*;

public class respond extends ListenerAdapter {
    private static final String COUNT_CHANNEL_ID = "1318277991398117457";
    private static final String DND_CHANNEL_ID = "1268086672420245556";
    private static final String TTS_CHANNEL_ID = "1207940576138371115";
    private static final int MAX_MESSAGE_LENGTH = 2000;

    // AI calls take seconds; running them on JDA's event thread stalls the whole bot
    private static final ExecutorService AI_POOL = Executors.newFixedThreadPool(4);
    // Single thread so TTS messages are generated and played in order
    private static final ExecutorService TTS_POOL = Executors.newSingleThreadExecutor();

    int count = 0;
    private static volatile String answer = null;
    private static volatile boolean trivia = false;
    private static volatile String triviaChannelID = null;
    public static boolean dnd = false;
    public static FileUpload uploadedImage;
    public static StringBuilder story = new StringBuilder();
    public static long id = 0;
    public static countTracker cT = new countTracker();
    public static boolean secondPart = false; // no longer used; kept in case other classes reference it

    public void trivia(String answer, String channelID) {
        respond.answer = answer;
        triviaChannelID = channelID;
        trivia = true;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message msg = event.getMessage();
        String message = msg.getContentRaw();
        MessageChannel channel = event.getChannel();
        String channelId = channel.getId();
        String selfId = event.getJDA().getSelfUser().getId();
        String botMention = "<@" + selfId + ">";
        boolean mentionsBot = message.contains(botMention) || message.contains("<@!" + selfId + ">");
        boolean isDndChannel = channelId.equals(DND_CHANNEL_ID);
        Member member = event.getMember();

        System.out.println(message);
        if (member != null) {
            System.out.println("message received");
            messageDB.addMessage(member.getIdLong());
        }

        // Counting channel
        if (channelId.equals(COUNT_CHANNEL_ID) && !event.getAuthor().isBot()) {
            int highScore = cT.messageIn(message); // call once; calling twice changes the tracker's state
            if (highScore != -1) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Count Restarted");
                eb.setColor(Color.red);
                eb.setDescription("High Score: " + highScore);
                channel.sendMessageEmbeds(eb.build()).queue();
            }
        }

        // D&D story log
        if (dnd && isDndChannel) {
            long authorId = event.getAuthor().getIdLong();
            if (authorId != id) {
                if (id != 0) {
                    story.append("----------------------------------------------------------------------------------------------------------").append("\n");
                }
                id = authorId;
            }
            if (event.getAuthor().isBot()) {
                story.append("DM").append(":    ").append(message).append("\n");
            } else if (message.trim().equals(botMention)) {
                story.append("The story continues. \n");
            } else {
                story.append(nameOf(msg)).append(":    ").append(message).append("\n");
            }
        }

        // Auto TTS (skip messages with no text, e.g. image-only, which ElevenLabs rejects)
        System.out.println(channelId);
        if (channelId.equals(TTS_CHANNEL_ID) && autoTTS && event.isFromGuild() && !message.isBlank()) {
            var guild = event.getGuild();
            TTS_POOL.submit(() -> {
                try {
                    if (AIc.ttsCall(message, "output")) {
                        PlayerManager.get().play(guild, "output.mp3");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            });
        }

        // Everything below responds to messages, so ignore bots (including ourselves) to prevent loops
        if (event.getAuthor().isBot()) {
            return;
        }

        if (member != null) {
            try {
                inv.addUser(member.getIdLong());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Message referenced = msg.getReferencedMessage();
        boolean replyingToBot = referenced != null && referenced.getAuthor().getId().equals(selfId);

        if (isDndChannel) {
            if (dnd && mentionsBot) {
                runAsync(() -> continueStory(channel, msg));
            }
        } else if (mentionsBot) {
            runAsync(() -> respondToMention(channel, msg, message));
        } else if (replyingToBot) {
            runAsync(() -> respondToReply(channel, msg, referenced, message));
        }

        if (trivia && channelId.equals(triviaChannelID)) {
            if (answer != null && answer.trim().equalsIgnoreCase(message.trim())) {
                trivia = false;
                msg.addReaction(Emoji.fromUnicode("U+2705")).queue();
                msg.reply("Correct!").queue();
            } else {
                msg.addReaction(Emoji.fromUnicode("U+274C")).queue();
            }
        } else if (!isDndChannel) {
            int ran = (int) (Math.random() * 40);
            System.out.println(ran);
            if (ran == 5 && !mentionsBot && !replyingToBot && settingsDB.getState("randReply")) {
                runAsync(() -> randomReply(channel, msg));
            }

            if (event.getAuthor().getName().equals("frankie4sd")) {
                count++;
                int rand_int1 = rand.nextInt(30);
                if (rand_int1 == 3 && settingsDB.getState("frankie")) {
                    msg.reply("https://tenor.com/view/shh-gif-27680056").queue();
                }
            }
        }
    }

    // Runs a task off the event thread and logs failures instead of losing them
    private static void runAsync(Runnable task) {
        AI_POOL.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void continueStory(MessageChannel channel, Message trigger) {
        System.out.println(channel.getName());
        List<Message> history = channel.getHistoryBefore(trigger.getId(), 25).complete().getRetrievedHistory();
        StringBuilder ss = new StringBuilder();
        for (int i = history.size() - 1; i >= 0; i--) {
            Message m = history.get(i);
            if (!m.getContentDisplay().isEmpty()) {
                ss.append(nameOf(m)).append(": ").append(m.getContentDisplay()).append("\n");
            }
        }
        if (!trigger.getContentDisplay().isEmpty()) {
            ss.append(nameOf(trigger)).append(": ").append(trigger.getContentDisplay()).append("\n");
        }
        System.out.println(ss);

        String out = AIc.gptCall("Continue the story with one message, do not include \"EGCBOT:\" or any other names in that style. It must be under 2000 characters in length: " + ss, textModel);

        // Unique file per request so it can't collide with /dalle or another story turn
        String baseName = "story-" + UUID.randomUUID();
        File img = new File(baseName + ".png");
        FileUpload image = null;
        try {
            AIc.dalleCall(AIc.gptCall("Turn this into a short pg dalle prompt: " + out, textModel), baseName);
            if (img.exists()) {
                image = FileUpload.fromData(img, "image.png");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        uploadedImage = image;

        sendLong(channel, out, image, img);
    }

    private void respondToMention(MessageChannel channel, Message msg, String message) {
        System.out.println(channel.getName());

        Message.Attachment image = firstImage(msg);
        if (image == null && msg.getReferencedMessage() != null) {
            image = firstImage(msg.getReferencedMessage());
        }
        if (image != null) {
            System.out.println("has attachments");
            sendVision(channel, image, message);
            return;
        }

        StringBuilder ss = buildHistory(channel, msg.getId(), 40);
        appendNewest(ss, msg);
        System.out.println(ss);
        sendLong(channel, AIc.gptCall("Respond to this message as yourself, EGCBot, with a short response: " + message + ". Do not mention your name. Dont ask questions. Here is the context to that message: " + ss, textModel), null);
    }

    private void respondToReply(MessageChannel channel, Message msg, Message referenced, String message) {
        StringBuilder ss = buildHistory(channel, referenced.getId(), 40);
        if (!referenced.getContentDisplay().isEmpty()) {
            ss.append("\n(Message being replied to): ").append(nameOf(referenced)).append(": ").append(referenced.getContentDisplay());
        }
        appendNewest(ss, msg);
        System.out.println(ss);
        sendLong(channel, AIc.gptCall("Respond to this message as yourself, EGCBot, with a short response: " + message + ". Do not mention your name. Dont ask questions. Here is the context to that message: " + ss, textModel), null);
    }

    private void randomReply(MessageChannel channel, Message msg) {
        System.out.println(channel.getName());
        StringBuilder ss = buildHistory(channel, msg.getId(), 40);
        appendNewest(ss, msg);
        sendLong(channel, AIc.gptCall("Jump into this conversation as yourself, EGCBot, with a short response. Act like you were always part of the conversation. Do not mention your name. Dont ask questions: \n" + ss, textModel), null);
    }

    private static StringBuilder buildHistory(MessageChannel channel, String beforeId, int limit) {
        List<Message> history = channel.getHistoryBefore(beforeId, limit).complete().getRetrievedHistory();
        StringBuilder ss = new StringBuilder();
        for (int i = history.size() - 1; i >= 0; i--) {
            Message m = history.get(i);
            if (!m.getContentDisplay().isEmpty()) {
                ss.append("\n").append(i + 1).append(": ").append(nameOf(m)).append(": ").append(m.getContentDisplay());
            }
        }
        return ss;
    }

    private static void appendNewest(StringBuilder ss, Message m) {
        if (!m.getContentDisplay().isEmpty()) {
            ss.append("\n(Newest Message) 0: ").append(nameOf(m)).append(": ").append(m.getContentDisplay()).append("\n");
        }
    }

    // Nickname if set, otherwise display/user name; works for webhooks and users with no nickname
    private static String nameOf(Message m) {
        Member mem = m.getMember();
        return mem != null ? mem.getEffectiveName() : m.getAuthor().getName();
    }

    private static Message.Attachment firstImage(Message m) {
        for (Message.Attachment a : m.getAttachments()) {
            if (a.isImage()) {
                return a;
            }
        }
        return null;
    }

    private static void sendVision(MessageChannel channel, Message.Attachment image, String prompt) {
        // Keep the real extension: visionCall builds the MIME type from it,
        // so a JPEG saved as .png would be sent as image/png
        String ext = image.getFileExtension();
        if (ext == null || ext.isBlank()) {
            ext = "png";
        }

        File tmp;
        try {
            tmp = File.createTempFile("vision", "." + ext); // unique file so simultaneous requests don't overwrite each other
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        image.getProxy().downloadToFile(tmp)
                .thenAccept(file -> {
                    try {
                        sendLong(channel, AIController.visionCall(prompt, file.getPath()), null);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e); // lambdas can't throw checked exceptions
                    }
                })
                .whenComplete((v, t) -> {
                    if (t != null) {
                        t.printStackTrace();
                    }
                    tmp.delete();
                });
    }

    private static void sendLong(MessageChannel channel, String text, FileUpload file) {
        sendLong(channel, text, file, null);
    }

    // Splits text into Discord-sized chunks; attaches the file (if any) to the last chunk,
    // then deletes cleanupFile (if given) once the send finishes
    private static void sendLong(MessageChannel channel, String text, FileUpload file, File cleanupFile) {
        Runnable cleanup = () -> {
            if (cleanupFile != null) {
                cleanupFile.delete();
            }
        };

        if (text == null || text.isBlank()) {
            if (file != null) {
                channel.sendFiles(file).queue(s -> cleanup.run(), f -> cleanup.run());
            } else {
                cleanup.run();
            }
            return;
        }
        List<String> parts = splitMessage(text);
        for (int i = 0; i < parts.size(); i++) {
            MessageCreateAction action = channel.sendMessage(parts.get(i));
            if (i == parts.size() - 1) {
                if (file != null) {
                    action = action.addFiles(file);
                }
                action.queue(s -> cleanup.run(), f -> cleanup.run());
            } else {
                action.queue();
            }
        }
    }

    private static List<String> splitMessage(String text) {
        List<String> parts = new ArrayList<>();
        while (text.length() > MAX_MESSAGE_LENGTH) {
            int cut = text.lastIndexOf('\n', MAX_MESSAGE_LENGTH);
            if (cut <= 0) {
                cut = text.lastIndexOf(' ', MAX_MESSAGE_LENGTH);
            }
            if (cut <= 0) {
                cut = MAX_MESSAGE_LENGTH;
            }
            parts.add(text.substring(0, cut));
            text = text.substring(cut).stripLeading();
        }
        if (!text.isEmpty()) {
            parts.add(text);
        }
        return parts;
    }
}