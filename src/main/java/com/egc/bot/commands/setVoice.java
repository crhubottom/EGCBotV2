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
        String voice=ctx.getOption("voice").getAsString();
        int voiceNum=-1;

        if(!voice.contains(",")) {
            try{
                voiceNum=Integer.parseInt(voice);
            }catch (NumberFormatException e){
                ctx.getHook().sendMessage("Invalid voice number").queue();
                return;
            }
            if (voiceNum==-1) {
                currentVoice.clear();
                currentVoice.add("Random");
                ctx.getHook().sendMessage("Voice set to random").queue();
                return;
            }
            if (voiceNum < 0 || voiceNum >= voiceArray.length) {
                ctx.getHook().sendMessage("Voice does not exist").queue();
                return;
            }
            currentVoice.clear();
            currentVoice.add(voiceArray[voiceNum].name);
            ctx.getHook().sendMessage("Voice set to " + currentVoice).queue();
        }else{
            ArrayList<Integer> numbers = new ArrayList<>();

            String[] parts = voice.split(",");

            for (String part : parts) {
                try {
                    numbers.add(Integer.parseInt(part));
                }catch (NumberFormatException e) {
                    ctx.getHook().sendMessage("Invalid voice number: " + part).queue();
                    return;
                }
            }
                currentVoice.clear();
            for (Integer number : numbers) {
                currentVoice.add(voiceArray[number].name);
            }
            ctx.getHook().sendMessage("Voices set to " + currentVoice).queue();
        }
    }
}
