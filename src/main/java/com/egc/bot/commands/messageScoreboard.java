package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.messageDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;

import static com.egc.bot.Bot.inv;

public class messageScoreboard implements ICommand {
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        ctx.getHook().sendMessageEmbeds(messageDB.getScoreboard().build()).queue();
    }


}