package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.tipDB;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class tipCounter implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        ctx.getHook().sendMessage(tipDB.getCount().toString()).queue();
    }
}
