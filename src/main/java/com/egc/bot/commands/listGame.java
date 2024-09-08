package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class listGame implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();

    }
}
