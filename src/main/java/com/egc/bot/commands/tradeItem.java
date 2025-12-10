package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;

import static com.egc.bot.Bot.*;
import static com.egc.bot.Bot.inv;

public class tradeItem implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        String username;
        long id;
        if(ctx.getOption("user")==null||ctx.getOption("user").getAsString().isEmpty()||ctx.getOption("youritem")==null||ctx.getOption("youritem").getAsString().isEmpty()||ctx.getOption("yourcount")==null||ctx.getOption("yourcount").getAsString().isEmpty()||ctx.getOption("theiritem")==null||ctx.getOption("theiritem").getAsString().isEmpty()||ctx.getOption("theircount")==null||ctx.getOption("theircount").getAsString().isEmpty()) {
            ctx.getHook().sendMessage("You must fill all fields.").queue();
            return;
        }else{
            if (client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).isEmpty()) {
                ctx.getHook().sendMessage("User not found.").queue();
                return;
            }else{
                username = client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).get(0).getNickname();
                id=client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).get(0).getIdLong();

            }
        }
        try {
            if(ctx.getOption("yourcount").getAsInt()==0&&ctx.getOption("theircount").getAsInt()==0){
                ctx.getHook().sendMessage("You can not trade nothing.").queue();
                return;
            }else if(ctx.getOption("theircount").getAsInt()==0){
                if (!inv.checkItem(ctx.getMember().getIdLong(), ctx.getOption("youritem").getAsString(), ctx.getOption("yourcount").getAsInt())) {
                    ctx.getHook().sendMessage("You dont have enough " + ctx.getOption("youritem").getAsString() + " to trade.").queue();
                    return;
                }else{
                    traderID=ctx.getMember().getIdLong();
                    receiverID=client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).get(0).getIdLong();
                    traderItem=ctx.getOption("youritem").getAsString();
                    traderCount=ctx.getOption("yourcount").getAsInt();
                    receiverItem=ctx.getOption("theiritem").getAsString();
                    receiverCount=ctx.getOption("theircount").getAsInt();
                    System.out.println(receiverID+": "+client.getGuildById(guildID).getMemberById(receiverID).getNickname());
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle( ctx.getMember().getNickname()+" <--> "+username, null);
                    eb.setColor(Color.red);
                    eb.setDescription(ctx.getMember().getNickname()+" offers:\n"+ctx.getOption("youritem").getAsString()+": " +ctx.getOption("yourcount").getAsInt()+"\n For "+username+"'s:\n"+ctx.getOption("theiritem").getAsString()+": "+ctx.getOption("theircount").getAsInt()+"\n\n Awaiting Confirmation.");ctx.getHook()
                            .sendMessageEmbeds(eb.build())
                            .setComponents(
                                    ActionRow.of(
                                            Button.success("acceptTrade", "Accept"),
                                            Button.danger("denyTrade", "Deny")
                                    )
                            )
                            .queue();
                    return;

                }
            }else if(ctx.getOption("yourcount").getAsInt()==0){
                if (!inv.checkItem(id, ctx.getOption("theiritem").getAsString(), ctx.getOption("theircount").getAsInt())) {
                    ctx.getHook().sendMessage(username+" does not have enough " + ctx.getOption("theiritem").getAsString() + " to trade.").queue();
                    return;
                }else{
                    traderID=ctx.getMember().getIdLong();
                    receiverID=client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).get(0).getIdLong();
                    traderItem=ctx.getOption("youritem").getAsString();
                    traderCount=ctx.getOption("yourcount").getAsInt();
                    receiverItem=ctx.getOption("theiritem").getAsString();
                    receiverCount=ctx.getOption("theircount").getAsInt();
                    System.out.println(receiverID+": "+client.getGuildById(guildID).getMemberById(receiverID).getNickname());
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle( ctx.getMember().getNickname()+" <--> "+username, null);
                    eb.setColor(Color.red);
                    eb.setDescription(ctx.getMember().getNickname()+" offers:\n"+ctx.getOption("youritem").getAsString()+": " +ctx.getOption("yourcount").getAsInt()+"\n For "+username+"'s:\n"+ctx.getOption("theiritem").getAsString()+": "+ctx.getOption("theircount").getAsInt()+"\n\n Awaiting Confirmation.");
                    ctx.getHook()
                            .sendMessageEmbeds(eb.build())
                            .setComponents(
                                    ActionRow.of(
                                            Button.success("acceptTrade", "Accept"),
                                            Button.danger("denyTrade", "Deny")
                                    )
                            )
                            .queue();
                    return;

                }
            }else if (!inv.checkItem(ctx.getMember().getIdLong(), ctx.getOption("youritem").getAsString(), ctx.getOption("yourcount").getAsInt())) {
                ctx.getHook().sendMessage("You dont have enough "+ctx.getOption("youritem").getAsString()+" to trade.").queue();
                return;
            }else{
                if (!inv.checkItem(id, ctx.getOption("theiritem").getAsString(), ctx.getOption("theircount").getAsInt())) {
                    ctx.getHook().sendMessage(username+" does not have enough " + ctx.getOption("theiritem").getAsString() + " to trade.").queue();
                    return;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        if(ctx.getOption("theircount").getAsInt()<0||ctx.getOption("yourcount").getAsInt()<0){
            ctx.getHook().sendMessage("You must enter positive numbers.").queue();
            return;
        }
        traderID=ctx.getMember().getIdLong();
        receiverID=client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).get(0).getIdLong();
        traderItem=ctx.getOption("youritem").getAsString();
        traderCount=ctx.getOption("yourcount").getAsInt();
        receiverItem=ctx.getOption("theiritem").getAsString();
        receiverCount=ctx.getOption("theircount").getAsInt();
        System.out.println(receiverID+": "+client.getGuildById(guildID).getMemberById(receiverID).getNickname());
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle( ctx.getMember().getNickname()+" <--> "+username, null);
        eb.setColor(Color.red);
        //eb.setColor(new Color(0xF40C0C));
        //eb.setColor(new Color(255, 0, 54));

        eb.setDescription(ctx.getMember().getNickname()+" offers:\n"+ctx.getOption("youritem").getAsString()+": " +ctx.getOption("yourcount").getAsInt()+"\n For "+username+"'s:\n"+ctx.getOption("theiritem").getAsString()+": "+ctx.getOption("theircount").getAsInt()+"\n\n Awaiting Confirmation.");
        ctx.getHook()
                .sendMessageEmbeds(eb.build())
                .setComponents(
                        ActionRow.of(
                                Button.success("acceptTrade", "Accept"),
                                Button.danger("denyTrade", "Deny")
                        )
                )
                .queue();

    }


}