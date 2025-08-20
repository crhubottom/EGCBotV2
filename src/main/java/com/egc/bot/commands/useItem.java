package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;
import java.sql.SQLException;

import static com.egc.bot.Bot.store;

public class useItem implements ICommand {
    public void run(SlashCommandInteraction ctx) throws SQLException, InterruptedException {
        ctx.deferReply().queue();
        if(ctx.getOption("item").getAsString().isEmpty()||ctx.getOption("item")==null){
            ctx.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("Error").setDescription("Please specify a valid item!").setColor(Color.red).build()).queue();
        }else if(ctx.getOption("amount")==null){
            ctx.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle(ctx.getOption("item").getAsString()).setDescription(store.buy(ctx.getOption("item").getAsString(),ctx.getMember().getIdLong(),1)).setColor(Color.yellow).build()).queue();
        }else if(ctx.getOption("amount").getAsInt()<=0){
            ctx.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("Error").setDescription("Amount must be greater than 0.").setColor(Color.red).build()).queue();
        }else{
            ctx.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle(ctx.getOption("item").getAsString()).setDescription(store.buy(ctx.getOption("item").getAsString(),ctx.getMember().getIdLong(),ctx.getOption("amount").getAsInt())).setColor(Color.yellow).build()).queue();

        }
    }


}