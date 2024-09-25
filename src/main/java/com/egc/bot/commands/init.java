package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.messageDB;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class init implements ICommand {
    public void run(SlashCommandInteraction ctx){
        ctx.deferReply().queue();
        long id= Long.parseLong("205713886601674753");
        if(ctx.getUser().getIdLong()==id) {
            messageDB.init(Long.parseLong(ctx.getOption("id").getAsString()), ctx.getOption("count").getAsInt());
            ctx.getHook().sendMessage("ok").queue();
        }else{
            ctx.getHook().sendMessage("no").queue();
        }
    }
}
