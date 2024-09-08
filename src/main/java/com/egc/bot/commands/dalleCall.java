package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import io.github.stefanbratanov.jvm.openai.OpenAIException;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.net.MalformedURLException;

import static com.egc.bot.Bot.AIc;

public class dalleCall implements ICommand {
    public void run(SlashCommandInteraction ctx) throws MalformedURLException {
        ctx.deferReply().queue();
        try {
            AIc.dalleCall(ctx.getOption("prompt").getAsString(),"image");
        java.io.File a=new File("image.png");
        FileUpload upload = FileUpload.fromData(a, "image.png");
        ctx.getHook().sendFiles(upload).queue();
        }catch (OpenAIException e){
            ctx.getHook().sendMessage(e.toString()).queue();
        }


    }

}