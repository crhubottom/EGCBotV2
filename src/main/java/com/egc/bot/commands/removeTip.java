package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.tipDB;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class removeTip implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        if(ctx.getOption("id").getAsString()==null){
            ctx.getHook().sendMessage("You must enter an id.").queue();
            return;
        }
        int id=ctx.getOption("id").getAsInt();

        boolean removed = tipDB.removeTip(id);
        String message;
        if (removed) {
            message = "Tip removed.";
        } else {
            message = "Error, tip not removed";
        }
        ctx.getHook().sendMessage(message).queue();
    }
}
