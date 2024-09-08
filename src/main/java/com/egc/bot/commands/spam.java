package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

/**
 * Basic Ping command
 */
public class spam implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        String message=ctx.getOption("text").getAsString();
        int count=ctx.getOption("count").getAsInt();
            System.out.println(message);
            ctx.getHook().sendMessage("Starting spam").queue();
            for (int y = 0; y < count; y++) {

                ctx.getChannel().sendMessage(message+" ("+(count-y)+" left)").queue();

        }
    }
}
