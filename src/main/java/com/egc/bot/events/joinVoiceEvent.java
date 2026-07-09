package com.egc.bot.events;

import com.egc.bot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
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
        // Only react to a "fresh" join: joined a channel and wasn't in one before.
        if (event.getChannelJoined() == null || event.getChannelLeft() != null) {
            return;
        }

        final String memberName = event.getMember().getEffectiveName();
        final String memberId = event.getMember().getId();
        final boolean isBot = memberId.equals(keys.get("BOT_ID"));

        System.out.println(memberName + " joined " + event.getChannelJoined().getName());

        // Guard against a missing/unreachable welcome channel.
        TextChannel tc = event.getGuild().getTextChannelById(WELCOME_CHANNEL_ID);
        if (tc == null) {
            System.err.println("Welcome channel " + WELCOME_CHANNEL_ID + " not found.");
            return;
        }

        // retrieveMessageById / getHistoryBefore are blocking REST calls. Run them off
        // the JDA gateway thread so we don't stall event dispatch. (Swap the raw Thread
        // for your own executor if you have one.)
        new Thread(() -> handleJoin(tc, memberName, memberId, isBot),
                "voice-welcome-" + memberId).start();
    }

    private void handleJoin(TextChannel tc, String memberName, String memberId, boolean isBot) {
        // If the channel has never had a message, there's nothing to read.
        List<String> possibleOutputGeneral = new ArrayList<>();
        List<String> possibleOutputBot = new ArrayList<>();
        List<String> possibleOutputPersonalized = new ArrayList<>();

        try {
            Message latestMessage = tc.retrieveMessageById(tc.getLatestMessageId()).complete();
            MessageHistory history = tc.getHistoryBefore(tc.getLatestMessageId(), 100).complete();

            List<Message> messages = new ArrayList<>();
            messages.add(latestMessage); // newest first
            messages.addAll(history.getRetrievedHistory());

            for (Message m : messages) {
                String content = m.getContentRaw();

                if (content.contains("Bot")) {
                    possibleOutputBot.add(buildMessage(content, memberName));
                } else if (content.contains("^")) {
                    if (content.contains("{") && content.contains("}")) {
                        // Personalized line: only usable if this member is named in { ... }.
                        if (nameInBraces(content, memberName)) {
                            possibleOutputPersonalized.add(buildMessage(content, memberName));
                        }
                    } else {
                        // General line: applies to everyone.
                        possibleOutputGeneral.add(buildMessage(content, memberName));
                    }
                }
                // Messages with no "^" and not a Bot line are ignored.
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to read welcome messages: " + e.getMessage());
            return;
        }

        // Decide which line to speak.
        String chosen;
        if (isBot) {
            chosen = pick(possibleOutputBot);
        } else if (!possibleOutputPersonalized.isEmpty() && !possibleOutputGeneral.isEmpty()) {
            // Both available: coin flip between them.
            chosen = (rand.nextInt(2) == 0)
                    ? pick(possibleOutputGeneral)
                    : pick(possibleOutputPersonalized);
        } else if (!possibleOutputPersonalized.isEmpty()) {
            chosen = pick(possibleOutputPersonalized);
        } else {
            chosen = pick(possibleOutputGeneral); // null if this is empty too
        }

        // No matching line -> stay silent instead of playing a stale welcome.mp3.
        if (chosen == null) {
            System.out.println("No welcome line available for " + memberName);
            return;
        }

        System.out.println("Speaking: " + chosen);

        try {
            // Unique file per member so simultaneous joins don't clobber each other's audio.
            String fileBase = "welcome_" + memberId;
            AIc.ttsCall(chosen, fileBase);
            PlayerManager.get().play(client.getGuildById(guildID), fileBase + ".mp3");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
    /**
     * Builds an output line from a template.
     * '^' (when it appears before '{') is replaced with the member's name.
     * Everything from '{' onward is metadata and is dropped.
     * Safe against a missing '^' or '{'.
     */
    private static String buildMessage(String content, String name) {
        int brace = content.indexOf('{');
        int end = (brace == -1) ? content.length() : brace;
        int caret = content.indexOf('^');

        if (caret != -1 && caret < end) {
            return content.substring(0, caret) + name + content.substring(caret + 1, end);
        }
        return content.substring(0, end);
    }

    /**
     * Returns true if {@code name} exactly matches one of the comma-separated
     * entries inside the {@code { ... }} block. Uses equals (not contains) so
     * "Al" no longer matches "{Alice}".
     */
    private static boolean nameInBraces(String content, String name) {
        int open = content.indexOf('{');
        int close = content.indexOf('}');
        if (open == -1 || close == -1 || close < open) {
            return false;
        }
        String list = content.substring(open + 1, close);
        for (String token : list.split(",")) {
            if (token.trim().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /** Random element, or null if the list is empty (avoids nextInt(0) crashing). */
    private static <T> T pick(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(rand.nextInt(list.size()));
    }
}