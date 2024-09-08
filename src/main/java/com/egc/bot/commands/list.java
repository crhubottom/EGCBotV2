package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.tipDB;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.util.ArrayList;

public class list implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        ArrayList<StringBuilder> arList = tipDB.list();
        String list;
        if(ctx.getOption("page")==null){
             list=arList.get(arList.size()-1).toString();
        }else if(ctx.getOption("page").getAsInt()>arList.size()||ctx.getOption("page").getAsInt()==0){
            ctx.getHook().sendMessage("Invalid Page. There are "+(arList.size())+" pages of tips.").queue();
            return;
        }else{
             list = tipDB.list().get(ctx.getOption("page").getAsInt()-1).toString();
        }
        System.out.println("list:"+list);
        ctx.getHook().sendMessage(list).queue();
    }
}
