package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.tipDB;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.util.ArrayList;

import static com.egc.bot.Bot.currentVoice;
import static com.egc.bot.Bot.voiceArray;

public class setVoice implements ICommand {
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        int voiceNum=ctx.getOption("voice").getAsInt();
        if(voiceNum<0||voiceNum>=voiceArray.length){
            ctx.getHook().sendMessage("Voice does not exist").queue();
            return;
        }
        currentVoice=voiceArray[voiceNum].name;
        ctx.getHook().sendMessage("Voice set to "+currentVoice).queue();
    }
}
