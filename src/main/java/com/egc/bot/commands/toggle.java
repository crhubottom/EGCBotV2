package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.settingsDB;
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
                ctx.getHook().sendMessage(settingsDB.toggleState("voiceTip")).queue();
                break;
            case "frankie":
                ctx.getHook().sendMessage(settingsDB.toggleState("frankie")).queue();
                break;
            case "reply":
                ctx.getHook().sendMessage(settingsDB.toggleState("randReply")).queue();
                break;
            case "all":
                ctx.getHook().sendMessage(settingsDB.toggleAll()).queue();
                break;
            default:
                ctx.getHook().sendMessage("Incorrect option. Options are \"reply\", \"frankie\", \"tip\", \"all\"").queue();
                break;
        }
    }
}