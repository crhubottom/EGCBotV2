package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.events.tipEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.io.IOException;

/**
 * Basic Ping command
 */
public class tip implements ICommand {

    public void run(SlashCommandInteraction ctx) throws IOException, InterruptedException {
        ctx.deferReply().queue();
        if (!ctx.getMember().getVoiceState().inAudioChannel()) {
            ctx.getHook().sendMessage("You need to be in a voice channel").queue();
            return;
        }
                tipEvent tipE=new tipEvent();
                ctx.getHook().sendMessage(tipE.tip()).queue();

    }
}




