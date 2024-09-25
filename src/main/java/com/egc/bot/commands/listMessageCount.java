package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.messageDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;
import java.sql.SQLException;

import static com.egc.bot.Bot.*;

public class listMessageCount implements ICommand {
    public void run(SlashCommandInteraction ctx) throws SQLException {
        ctx.deferReply().queue();
        String username = ctx.getMember().getNickname();
        long id=ctx.getMember().getIdLong();
        if(ctx.getOption("user")!=null&&!ctx.getOption("user").getAsString().isEmpty()) {
            if (!client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).isEmpty()) {
                username = client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).get(0).getNickname();
                id=client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).get(0).getIdLong();
            }else{
                ctx.getHook().sendMessage("User not found.").queue();
                return;
            }
        }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(username+ "'s Message Count", null);
            eb.setColor(new Color(27, 208, 229));
            eb.setDescription(username+" has sent "+messageDB.getCount(id)+" messages.");
            ctx.getHook().sendMessageEmbeds(eb.build()).queue();

    }


}