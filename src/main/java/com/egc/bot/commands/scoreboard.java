package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;

import static com.egc.bot.Bot.AIc;
import static com.egc.bot.Bot.inv;

public class scoreboard implements ICommand {
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle( "Scoreboard (Gold)", null);
        eb.setColor(Color.yellow);
        eb.setDescription(inv.scoreboard());
        ctx.getHook().sendMessageEmbeds(eb.build()).queue();
    }


}