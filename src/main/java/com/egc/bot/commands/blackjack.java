package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.events.blackjackController;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.egc.bot.Bot.*;

public class blackjack implements ICommand {
    public void run(SlashCommandInteraction ctx) throws SQLException, InterruptedException {

        bj.reset();
        ctx.deferReply().queue();
        if(ctx.getOption("gold")==null){
            ctx.getHook().sendMessage("You must fill all fields.").queue();
            return;
        }

        if(ctx.getOption("gold").getAsInt()<1||ctx.getOption("gold").getAsInt()>10000){
            ctx.getHook().sendMessage("You must enter a correct amount of gold (1->10000).").queue();
            return;
        }
        if(inv.checkItem(ctx.getMember().getIdLong(),"Gold",ctx.getOption("gold").getAsInt())){
            blackjackID=ctx.getMember().getIdLong();
            ctx.getHook().sendMessageEmbeds(bj.start(ctx.getOption("gold").getAsInt(),ctx.getMember().getIdLong()).build())
                    .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.danger("stand", "Stand"), Button.success("hit", "Hit")).queue();
        }else{
            ctx.getHook().sendMessage("You don't have enough Gold").queue();

        }
    }


}