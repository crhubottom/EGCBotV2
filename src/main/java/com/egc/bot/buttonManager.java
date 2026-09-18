package com.egc.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static com.egc.bot.Bot.*;

public class buttonManager extends ListenerAdapter {
    // Only one trade button press can be processed at a time, so a trade can't run twice
    private static final Object TRADE_LOCK = new Object();

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getCustomId();
        if (buttonId == null) {
            return;
        }
        long clicker = event.getUser().getIdLong();

        switch (buttonId) {
            case "acceptIcon" -> acceptIcon(event);
            case "acceptTrade" -> acceptTrade(event, clicker);
            case "denyTrade" -> denyTrade(event, clicker);
            case "hit" -> hit(event, clicker);
            case "stand" -> stand(event, clicker);
            default -> { }
        }
    }

    private void acceptIcon(ButtonInteractionEvent event) {
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.MANAGE_SERVER)) {
            privateReply(event, "You need the Manage Server permission to change the icon.");
            return;
        }

        Guild guild = client.getGuildById(guildID);
        if (guild == null) {
            privateReply(event, "I couldn't find the server.");
            return;
        }

        Icon icon;
        try {
            icon = Icon.from(new File("icon.png"));
        } catch (IOException e) {
            e.printStackTrace();
            privateReply(event, "I couldn't read the new icon file.");
            return;
        }

        guild.getManager().setIcon(icon).queue(
                success -> System.out.println("Server icon updated"),
                error -> System.out.println("Icon update failed: " + error.getMessage()));

        // Remove the button so the icon can't be re-applied by clicking again
        event.editComponents().queue();
    }

    private void acceptTrade(ButtonInteractionEvent event, long clicker) {
        synchronized (TRADE_LOCK) {
            if (receiverID == 0L || clicker != receiverID) {
                privateReply(event, "This trade isn't for you, or it's no longer open.");
                return;
            }

            System.out.println("Accept: " + receiverID);
            try {
                inv.trade(traderID, receiverID, traderItem, receiverItem, traderCount, receiverCount);
            } catch (SQLException e) {
                e.printStackTrace();
                receiverID = 0L;
                event.editMessageEmbeds(embed("Trade Failed.", Color.red)).setComponents().queue();
                return;
            }

            receiverID = 0L; // close the trade so it can't be accepted twice
            event.editMessageEmbeds(embed("Trade Accepted.", Color.green)).setComponents().queue();
        }
    }

    private void denyTrade(ButtonInteractionEvent event, long clicker) {
        synchronized (TRADE_LOCK) {
            if (receiverID == 0L || clicker != receiverID) {
                privateReply(event, "This trade isn't for you, or it's no longer open.");
                return;
            }

            System.out.println("Deny: " + receiverID);
            receiverID = 0L;
            event.editMessageEmbeds(embed("Trade Denied.", Color.red)).setComponents().queue();
        }
    }

    private void hit(ButtonInteractionEvent event, long clicker) {
        if (clicker != blackjackID) {
            privateReply(event, "This isn't your game.");
            return;
        }
        event.deferEdit().queue();
        bj.hit(event.getMessage().getIdLong(), event.getChannelIdLong());
    }

    private void stand(ButtonInteractionEvent event, long clicker) {
        if (clicker != blackjackID) {
            privateReply(event, "This isn't your game.");
            return;
        }
        event.deferEdit().queue();
        try {
            bj.stand(event.getMessage().getIdLong(), event.getChannelIdLong());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Only the person who clicked sees this
    private static void privateReply(ButtonInteractionEvent event, String text) {
        event.reply(text).setEphemeral(true).queue();
    }

    private static MessageEmbed embed(String title, Color color) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title, null);
        eb.setColor(color);
        return eb.build();
    }
}