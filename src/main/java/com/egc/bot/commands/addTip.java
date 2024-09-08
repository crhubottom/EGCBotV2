package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.tipDB;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class addTip implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        int tipCount=0;
        if(ctx.getOption("tip").getAsString()==null){
            ctx.getHook().sendMessage("You must enter a tip.").queue();
            return;
        }
        if(ctx.getOption("game").getAsString()==null){
            ctx.getHook().sendMessage("You must enter a game.").queue();
            return;
        }

        String game= ctx.getOption("game").getAsString().toLowerCase().replaceAll("\\s","");
        String tip=  ctx.getOption("tip").getAsString();
        String username=ctx.getMember().getEffectiveName();
        while(tip.contains("/")){
            System.out.println(tip.substring(0,tip.indexOf("/")));
            tipDB.addTip(game,tip.substring(0,tip.indexOf("/")),username);
            tipCount++;
            tip=tip.substring(tip.indexOf("/")+1);
            System.out.println("tip:"+tip);
        }
        System.out.println(username);
        boolean added = tipDB.addTip(game,tip,username);
        tipCount++;
        String message;
        if (added) {
            if(tipCount==1) {
                message = "Tip added.";
            }else{
                message = tipCount+" Tips added for "+game+".";
            }
        } else {
            message = "Error, tip not added for "+game+".";
        }
        ctx.getHook().sendMessage(message).queue();
    }
}
