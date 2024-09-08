package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.gameDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

public class stats implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        String username;

        ArrayList<StringBuilder> arList;
        String list;

        if(ctx.getOption("username")==null) {
            username=ctx.getMember().getUser().getName().replaceAll("\\s+", "").replaceAll("[,.]", "");
            arList=gameDB.listUser(username);
        }else{
            username=ctx.getOption("username").getAsString().replaceAll("\\s+", "").replaceAll("[,.]", "");
            arList=gameDB.listUser(username);
        }

        if(ctx.getOption("page")==null){
             list=arList.get(0).toString();
        }else if(ctx.getOption("page").getAsInt()>arList.size()||ctx.getOption("page").getAsInt()==0){
            ctx.getHook().sendMessage("Invalid Page. There are "+(arList.size())+" pages of tips.").queue();
            return;
        }else{
             list = gameDB.listUser(username).get(ctx.getOption("page").getAsInt()-1).toString();
        }
        if(list.equals("no table")){
            ctx.getHook().sendMessage("User has not played any games yet.").queue();
            return;
        }
        System.out.println("list:"+list);
        //ctx.getHook().sendMessage(list).queue();
        username= Objects.requireNonNull(ctx.getMember()).getNickname();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.blue);
        eb.setTitle("Stats for "+username);
        eb.setDescription(list);
        ctx.getHook().sendMessageEmbeds(eb.build()).queue();

    }
}
