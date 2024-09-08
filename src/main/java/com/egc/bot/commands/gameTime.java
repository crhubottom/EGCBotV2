package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.gameDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;
import java.util.ArrayList;

public class gameTime implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        ArrayList<StringBuilder> arList = gameDB.list();
        String list;

        if(ctx.getOption("game")!=null){
            ctx.getHook().sendMessage("# Stats for "+ctx.getOption("game").getAsString()+"\n"+gameDB.listgame(ctx.getOption("game").getAsString())).queue();
            return;
        }
        if(ctx.getOption("page")==null){
             list=arList.get(0).toString();
        }else if(ctx.getOption("page").getAsInt()>arList.size()||ctx.getOption("page").getAsInt()==0){
            ctx.getHook().sendMessage("Invalid Page. There are "+(arList.size())+" pages of tips.").queue();
            return;
        }else{
             list = gameDB.list().get(ctx.getOption("page").getAsInt()-1).toString();
        }
        System.out.println("list:"+list);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Total Playtime Across All Users");
        eb.setDescription(list);
        eb.setColor(Color.blue);
        ctx.getHook().sendMessageEmbeds(eb.build()).queue();
    }
}
