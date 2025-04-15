package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import static com.egc.bot.Bot.AIc;

public class gptCall implements ICommand {
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        try {
            ctx.getHook().sendMessage(AIc.gptCall(ctx.getOption("message").getAsString(),"gpt-4.1")).queue();
        }catch (Exception e){
            ctx.getHook().sendMessage(e.toString()).queue();
        }
    }


}