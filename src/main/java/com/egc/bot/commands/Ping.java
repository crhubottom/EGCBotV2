package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

/**
 * Basic Ping command
 */
public class Ping implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        long time = System.currentTimeMillis();
        ctx.reply("Pong!")
            .flatMap(v ->
                ctx.getHook().editOriginalFormat("Pong! %d ms", System.currentTimeMillis() - time) // then edit original
        ).queue();
    }
}
