package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import static com.egc.bot.Bot.AIc;
import static com.egc.bot.Bot.store;

public class showStore implements ICommand {
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        ctx.getHook().sendMessageEmbeds(store.getStore().build()).queue();
    }


}