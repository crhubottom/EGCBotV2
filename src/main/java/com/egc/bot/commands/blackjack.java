package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.events.blackjackController;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.egc.bot.Bot.*;

public class blackjack implements ICommand {
    public void run(SlashCommandInteraction ctx) throws SQLException, InterruptedException {

        if(!ctx.getChannelId().equals("1269464838472597577")){
        int fineAmount;
        if(inv.checkItem(ctx.getMember().getIdLong(),"Gold",1000)){
            fineAmount = 1000;
            }else{
            fineAmount=inv.getGold(ctx.getMember().getIdLong());
        }
        inv.DeleteItem(ctx.getMember().getIdLong(),"Gold",fineAmount);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Illegal Gambling");
        eb.setColor(Color.red);
        eb.setDescription("You have been fined "+fineAmount+" gold.");
        ctx.replyEmbeds(eb.build()).queue();
        return;
        }
        bj.reset();
        ctx.deferReply().queue();
        if(ctx.getOption("gold")==null){
            ctx.getHook().sendMessage("You must fill all fields.").queue();
            return;
        }

        if(ctx.getOption("gold").getAsInt()<1){
            ctx.getHook().sendMessage("You must enter a correct amount of gold).").queue();
            return;
        }
        if (inv.checkItem(ctx.getMember().getIdLong(), "Gold", ctx.getOption("gold").getAsInt())) {
            blackjackID = ctx.getMember().getIdLong();

            MessageEmbed embed = bj.start(
                    ctx.getOption("gold").getAsInt(),
                    ctx.getMember().getIdLong()
            ).build();

            ctx.getHook()
                    .sendMessageEmbeds(embed)
                    .setComponents(
                            ActionRow.of(
                                    Button.danger("stand", "Stand"),
                                    Button.success("hit", "Hit")
                            )
                    )
                    .queue();
        } else {
            ctx.getHook().sendMessage("You don't have enough Gold").queue();
        }


    }


}