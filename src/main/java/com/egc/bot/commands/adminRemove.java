package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;
import java.sql.SQLException;

import static com.egc.bot.Bot.*;

public class adminRemove implements ICommand {

    public void run(SlashCommandInteraction ctx) throws SQLException {
        ctx.deferReply().queue();
        if(ctx.getMember().getId().equals("205713886601674753")){
            if(ctx.getOption("item")!=null&&ctx.getOption("amount")!=null&&ctx.getOption("user")!=null) {
                String item = ctx.getOption("item").getAsString();
                int amount = ctx.getOption("amount").getAsInt();
                String user = ctx.getOption("user").getAsString();
                long receiverID = client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).get(0).getIdLong();
                inv.DeleteItem(receiverID, item, amount);
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Removed " + amount + " " + item + " to " + user);
                eb.setColor(Color.green);
                ctx.getHook().sendMessageEmbeds(eb.build()).queue();
            }
        }else{
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("You are not Chase");
                eb.setColor(Color.red);
                ctx.getHook().sendMessageEmbeds(eb.build()).queue();
        }
    }
}

