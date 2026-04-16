package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;

import static com.egc.bot.Bot.*;

public class voices implements ICommand {
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        StringBuilder out = new StringBuilder();
        for(int i=0;i<voiceArray.length;i++){
            out.append(i).append(": ").append(voiceArray[i]).append("\n");
        }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Available Voices", null);
            eb.setColor(Color.red);
            eb.setColor(new Color(0xF40C0C));
            eb.setColor(new Color(255, 0, 54));
            eb.setDescription(out.toString());
            ctx.getHook().sendMessageEmbeds(eb.build()).queue();

    }


}