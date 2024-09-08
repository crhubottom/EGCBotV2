package com.egc.bot.events;

import com.egc.bot.commands.Command;
import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Listens and runs commands executed by the user
 */
public class SlashCommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent ctx) {
        ICommand command = Command.commands.get(ctx.getName());
        if (command == null) {
            System.out.printf("Unknown command %s used by %#s%n", ctx.getName(), ctx.getUser());
            return;
        }
        try {
            command.run(ctx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
