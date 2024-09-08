package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.util.Objects;

import static com.egc.bot.Bot.*;


public class toggle implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        StringBuilder ss = new StringBuilder();
        ctx.deferReply().queue();
        if(ctx.getOption("option")==null){
            ctx.getHook().sendMessage("You must enter an option to toggle.").queue();
            return;
        }
        String option= Objects.requireNonNull(ctx.getOption("option")).getAsString();
        switch(option){
            case "tip":
                if(voiceTip){
                    voiceTip=false;
                    ctx.getHook().sendMessage("Random tips toggled off.").queue();
                }else{
                    voiceTip=true;
                    ctx.getHook().sendMessage("Random tips toggled on.").queue();

                }
                break;

            case "frankie":
                if(frankieReply){
                    frankieReply=false;
                    ctx.getHook().sendMessage("Be Quiet Frankie toggled off.").queue();
                }else{
                    frankieReply=true;
                    ctx.getHook().sendMessage("Be Quiet Frankie toggled on.").queue();
                }
                break;
            case "reply":
                if(randReply){
                    randReply=false;
                    ctx.getHook().sendMessage("Random replies toggled off.").queue();
                }else{
                    randReply=true;
                    ctx.getHook().sendMessage("Random replies toggled on.").queue();
                }
                break;
            case "all":

                if(frankieReply){
                    frankieReply=false;
                    ss.append("Be Quiet Frankie toggled off.\n");
                }else{
                    frankieReply=true;
                    ss.append("Be Quiet Frankie toggled on.\n");
                }
                if(randReply){
                    randReply=false;
                    ss.append("Random replies toggled off.\n");

                }else{
                    randReply=true;
                    ss.append("Random replies toggled on.\n");
                }
                if(voiceTip){
                    voiceTip=false;
                    ss.append("Random tips toggled off.\n");

                }else{
                    voiceTip=true;
                    ss.append("Random tips toggled on.\n");
                }
                ctx.getHook().sendMessage(ss.toString()).queue();
                break;
            default:
                ctx.getHook().sendMessage("Incorrect option. Options are \"reply\", \"frankie\", \"tip\", \"all\"").queue();
                break;
        }
    }
}