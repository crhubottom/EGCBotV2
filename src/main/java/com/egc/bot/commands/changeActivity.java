package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import static com.egc.bot.Bot.AIc;
import static com.egc.bot.Bot.client;

public class changeActivity implements ICommand {
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        if(ctx.getOption("type").getAsString().isEmpty()||ctx.getOption("type")==null||ctx.getOption("status").getAsString().isEmpty()||ctx.getOption("status")==null){
            ctx.reply("You must fill all fields").queue();
            return;
        }
        if(ctx.getOption("type").getAsString().equals("playing")){
            client.getPresence().setActivity(Activity.playing(ctx.getOption("status").getAsString()));
        }else if(ctx.getOption("type").getAsString().equals("watching")){
            client.getPresence().setActivity(Activity.watching(ctx.getOption("status").getAsString()));

        }else if(ctx.getOption("type").getAsString().equals("listening")){
            client.getPresence().setActivity(Activity.listening(ctx.getOption("status").getAsString()));

        }else if(ctx.getOption("type").getAsString().equals("competing")){
            client.getPresence().setActivity(Activity.competing(ctx.getOption("status").getAsString()));
        }else{
            ctx.getHook().sendMessage("Error, incorrect type").queue();
            return;
        }
        ctx.getHook().sendMessage("Status changed").queue();
    }


}