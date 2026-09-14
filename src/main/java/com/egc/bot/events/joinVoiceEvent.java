package com.egc.bot.events;

import com.egc.bot.audio.AudioReceiveHandler;
import com.egc.bot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.egc.bot.Bot.*;

public class joinVoiceEvent extends ListenerAdapter {

    private static final String WELCOME_CHANNEL_ID = "1491968908805279844";

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() != null) {
            AudioReceiveHandler.removeUser(event.getMember().getIdLong());
        }

        // Only react to a fresh join, not movement between voice channels.
        if (event.getChannelJoined() == null || event.getChannelLeft() != null) {
            return;
        }

        final String memberName = event.getMember().getEffectiveName();
        final String memberId = event.getMember().getId();
        final boolean isBot = memberId.equals(keys.get("BOT_ID"));

        System.out.println(
                memberName + " joined " + event.getChannelJoined().getName()
        );

        TextChannel tc = event.getGuild()
                .getTextChannelById(WELCOME_CHANNEL_ID);

        if (tc == null) {
            System.err.println(
                    "Welcome channel " + WELCOME_CHANNEL_ID + " not found."
            );
            return;
        }

        // Reading channel history is a blocking REST operation, so perform it
        // outside JDA's gateway event thread.
        Thread welcomeThread = new Thread(
                () -> handleJoin(tc, memberName, memberId, isBot),
                "voice-welcome-" + memberId
        );

        welcomeThread.start();
    }

    private void handleJoin(
            TextChannel tc,
            String memberName,
            String memberId,
            boolean isBot
    ) {
        List<String> possibleOutputGeneral = new ArrayList<>();
        List<String> possibleOutputBot = new ArrayList<>();
        List<String> possibleOutputPersonalized = new ArrayList<>();

        try {
            /*
             * Fetch the latest available messages directly.
             *
             * This avoids using getLatestMessageId() as a history anchor. That
             * ID can occasionally become stale and cause Discord error 10008:
             * Unknown Message.
             *
             * An empty channel simply returns an empty list.
             */
            List<Message> messages = tc.getHistory()
                    .retrievePast(100)
                    .complete();

            for (Message message : messages) {
                String content = message.getContentRaw();

                if (content.contains("Bot")) {
                    possibleOutputBot.add(
                            buildMessage(content, memberName)
                    );
                } else if (content.contains("^")) {
                    if (content.contains("{") && content.contains("}")) {
                        // Personalized line: only use it when this member is
                        // named inside the metadata braces.
                        if (nameInBraces(content, memberName)) {
                            possibleOutputPersonalized.add(
                                    buildMessage(content, memberName)
                            );
                        }
                    } else {
                        // General line: applies to everyone.
                        possibleOutputGeneral.add(
                                buildMessage(content, memberName)
                        );
                    }
                }

                // Messages without "^" that are not Bot lines are ignored.
            }
        } catch (RuntimeException e) {
            System.err.println(
                    "Failed to read welcome messages from channel "
                            + tc.getId() + ": " + e.getMessage()
            );
            e.printStackTrace();
            return;
        }

        String chosen;

        if (isBot) {
            chosen = pick(possibleOutputBot);
        } else if (!possibleOutputPersonalized.isEmpty()
                && !possibleOutputGeneral.isEmpty()) {
            // Both types are available, so choose between the two categories.
            chosen = rand.nextInt(2) == 0
                    ? pick(possibleOutputGeneral)
                    : pick(possibleOutputPersonalized);
        } else if (!possibleOutputPersonalized.isEmpty()) {
            chosen = pick(possibleOutputPersonalized);
        } else {
            chosen = pick(possibleOutputGeneral);
        }

        if (chosen == null) {
            System.out.println(
                    "No welcome line available for " + memberName
            );
            return;
        }

        System.out.println("Speaking: " + chosen);

        try {
            // A unique filename prevents simultaneous joins from overwriting
            // each other's generated audio.
            String fileBase = "welcome_" + memberId;

            AIc.ttsCall(chosen, fileBase);
            PlayerManager.get().play(
                    client.getGuildById(guildID),
                    fileBase + ".mp3"
            );
        } catch (IOException e) {
            System.err.println(
                    "Failed to generate welcome audio for "
                            + memberName + ": " + e.getMessage()
            );
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(
                    "Welcome audio generation interrupted for " + memberName
            );
        }
    }

    /**
     * Builds an output line from a template.
     *
     * '^', when it appears before '{', is replaced with the member's name.
     * Everything beginning with '{' is treated as metadata and removed.
     */
    private static String buildMessage(String content, String name) {
        int brace = content.indexOf('{');
        int end = brace == -1 ? content.length() : brace;
        int caret = content.indexOf('^');

        if (caret != -1 && caret < end) {
            return content.substring(0, caret)
                    + name
                    + content.substring(caret + 1, end);
        }

        return content.substring(0, end);
    }

    /**
     * Determines whether the member's name exactly matches a comma-separated
     * entry inside the first { ... } block.
     */
    private static boolean nameInBraces(String content, String name) {
        int open = content.indexOf('{');
        int close = content.indexOf('}', open + 1);

        if (open == -1 || close == -1) {
            return false;
        }

        String names = content.substring(open + 1, close);

        for (String token : names.split(",")) {
            if (token.trim().equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a random item, or null when the list is empty.
     */
    private static <T> T pick(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }

        return list.get(rand.nextInt(list.size()));
    }

}