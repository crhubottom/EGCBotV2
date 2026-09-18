package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.events.blackjackController;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.Color;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.egc.bot.Bot.inv;

public class roulette implements ICommand {
    // Real roulette layout: pocket 0 is green, odd pockets are red, even pockets are white (18 red, 18 white, 1 green)
    private static final int POCKETS = 37;
    private static final int VISIBLE = 11;   // squares shown on screen
    private static final int ARROW = 5;      // index of the square under the arrow
    private static final int SPIN_FRAMES = 14;
    private static final int FRAME_MS = 1000; // stays under Discord's edit rate limit so the animation is smooth
    private static final int GREEN_MULTIPLIER = 36; // 35:1 plus the bet back
    private static final int COLOR_MULTIPLIER = 2;  // 1:1 plus the bet back
    private static final int MAX_BET = Integer.MAX_VALUE / GREEN_MULTIPLIER; // so a green win can't overflow

    private static final String RED = ":red_square:";
    private static final String WHITE = "⬜";
    private static final String GREEN = ":green_square:";
    private static final String BLACK = ":black_large_square:";
    private static final String TOP = BLACK.repeat(ARROW) + ":arrow_down:" + BLACK.repeat(VISIBLE - ARROW - 1) + " ";
    private static final String BOTTOM = BLACK.repeat(VISIBLE);

    private static final Object BET_LOCK = new Object();

    public void run(SlashCommandInteraction ctx) throws SQLException, InterruptedException {
        ctx.deferReply().queue();
        InteractionHook hook = ctx.getHook();
        long userId = ctx.getMember().getIdLong();

        if (!blackjackController.isBlackjackChannel(ctx.getChannel().getIdLong())) {
            int fineAmount;
            if (inv.checkItem(userId, "Gold", 1000)) {
                fineAmount = 1000;
            } else {
                fineAmount = inv.getGold(userId);
            }
            inv.DeleteItem(userId, "Gold", fineAmount);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Illegal Gambling");
            eb.setColor(Color.red);
            eb.setDescription("You have been fined " + fineAmount + " gold.");
            hook.sendMessageEmbeds(eb.build()).queue(); // reply was already deferred, so use the hook
            return;
        }

        OptionMapping goldOption = ctx.getOption("gold");
        OptionMapping colorOption = ctx.getOption("color");
        if (goldOption == null || colorOption == null || colorOption.getAsString().isBlank()) {
            hook.sendMessage("You must fill all fields.").queue();
            return;
        }

        String chosenColor = colorOption.getAsString().trim().toLowerCase();
        if (!chosenColor.equals("red") && !chosenColor.equals("green") && !chosenColor.equals("white")) {
            hook.sendMessage("You must enter a correct color.").queue();
            return;
        }

        long requested = goldOption.getAsLong();
        if (requested < 1) {
            hook.sendMessage("You must enter a correct amount of gold.").queue();
            return;
        }
        if (requested > MAX_BET) {
            hook.sendMessage("The maximum bet is " + MAX_BET + " gold.").queue();
            return;
        }
        int bet = (int) requested;

        // Take the bet up front so the same gold can't be bet twice while the wheel spins
        synchronized (BET_LOCK) {
            if (!inv.checkItem(userId, "Gold", bet)) {
                hook.sendMessage("You don't have enough Gold").queue();
                return;
            }
            inv.DeleteItem(userId, "Gold", bet);
        }

        int roll = ThreadLocalRandom.current().nextInt(POCKETS);
        String result = colorOf(roll);
        boolean won = result.equals(chosenColor);
        final int payout = won ? bet * (result.equals("green") ? GREEN_MULTIPLIER : COLOR_MULTIPLIER) : 0;

        // Spin at least one full lap, ending with the rolled pocket under the arrow
        int target = Math.floorMod(roll - ARROW, POCKETS) + POCKETS;

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Roulette    (" + bet + " gold on " + chosenColor + ")", null);
        eb.setColor(Color.white);
        eb.setDescription(wheel(0));
        hook.editOriginalEmbeds(eb.build()).queue();

        // Scheduled edits instead of Thread.sleep, so nothing is blocked while it spins
        long delay = 0;
        int lastOffset = 0;
        for (int i = 1; i <= SPIN_FRAMES; i++) {
            double progress = (double) i / SPIN_FRAMES;
            int offset = (int) Math.round(target * (1 - (1 - progress) * (1 - progress))); // slows down near the end
            if (offset == lastOffset) {
                continue;
            }
            lastOffset = offset;
            delay += FRAME_MS;
            eb.setDescription(wheel(offset));
            hook.editOriginalEmbeds(eb.build()).queueAfter(delay, TimeUnit.MILLISECONDS);
        }

        // Keep the wheel visible and add the result under it
        delay += 2000;
        String outcome = won
                ? "**You won " + (payout - bet) + " gold!**"
                : "**You lost " + bet + " gold.**";
        eb.setDescription(wheel(target) + "\n\nThe ball landed on **" + result + "**.\n" + outcome);
        eb.setColor(won ? Color.green : Color.red);

        // Pay out when the result is shown; also pay if the edit fails so a win is never lost
        Runnable pay = () -> {
            if (payout > 0) {
                try {
                    inv.AddItem(userId, "Gold", payout);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        hook.editOriginalEmbeds(eb.build()).queueAfter(delay, TimeUnit.MILLISECONDS,
                success -> pay.run(),
                error -> {
                    error.printStackTrace();
                    pay.run();
                });
    }

    private static String colorOf(int pocket) {
        if (pocket == 0) {
            return "green";
        }
        return pocket % 2 == 1 ? "red" : "white";
    }

    private static String pocketEmoji(int pocket) {
        switch (colorOf(Math.floorMod(pocket, POCKETS))) {
            case "green":
                return GREEN;
            case "red":
                return RED;
            default:
                return WHITE;
        }
    }

    // Draws the visible part of the wheel, rotated by offset pockets
    private static String wheel(int offset) {
        StringBuilder sb = new StringBuilder(TOP).append("\n");
        for (int i = 0; i < VISIBLE; i++) {
            sb.append(pocketEmoji(i + offset));
        }
        return sb.append("\n").append(BOTTOM).toString();
    }
}