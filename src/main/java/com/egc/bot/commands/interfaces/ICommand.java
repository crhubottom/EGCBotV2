package com.egc.bot.commands.interfaces;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

/**
 * Base command interface every command will implement
 */
public interface ICommand {
    void run(SlashCommandInteraction ctx) throws Exception;

}
