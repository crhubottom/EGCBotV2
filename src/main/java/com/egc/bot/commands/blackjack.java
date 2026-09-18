package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.events.blackjackController;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;
import java.sql.SQLException;

import static com.egc.bot.Bot.*;

public class blackjack implements ICommand {
    public void run(SlashCommandInteraction ctx) throws SQLException, InterruptedException {

        if (!blackjackController.isBlackjackChannel(ctx.getChannel().getIdLong())) {
            int fineAmount;
            if (inv.checkItem(ctx.getMember().getIdLong(), "Gold", 1000)) {
                fineAmount = 1000;
            } else {
                fineAmount = inv.getGold(ctx.getMember().getIdLong());
            }
            inv.DeleteItem(ctx.getMember().getIdLong(), "Gold", fineAmount);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Illegal Gambling");
            eb.setColor(Color.red);
            eb.setDescription("You have been fined " + fineAmount + " gold.");
            ctx.replyEmbeds(eb.build()).queue();
            return;
        }

        // Checked before deferring so the reply can be private
        if (bj.isInProgress()) {
            ctx.reply("A game is already in progress. Wait for it to finish.").setEphemeral(true).queue();
            return;
        }

        ctx.deferReply().queue();
        if (ctx.getOption("gold") == null) {
            ctx.getHook().sendMessage("You must fill all fields.").queue();
            return;
        }

        int bet = ctx.getOption("gold").getAsInt();
        if (bet < 1) {
            ctx.getHook().sendMessage("You must enter a correct amount of gold.").queue();
            return;
        }
        if (!inv.checkItem(ctx.getMember().getIdLong(), "Gold", bet)) {
            ctx.getHook().sendMessage("You don't have enough Gold").queue();
            return;
        }

        // start() refuses (returns null) if someone else started a game in the meantime
        EmbedBuilder game = bj.start(bet, ctx.getMember().getIdLong());
        if (game == null) {
            ctx.getHook().sendMessage("A game is already in progress. Wait for it to finish.").queue();
            return;
        }
        blackjackID = ctx.getMember().getIdLong();

        ctx.getHook()
                .sendMessageEmbeds(game.build())
                .setComponents(
                        ActionRow.of(
                                Button.danger("stand", "Stand"),
                                Button.success("hit", "Hit")
                        )
                )
                .queue();
    }
}