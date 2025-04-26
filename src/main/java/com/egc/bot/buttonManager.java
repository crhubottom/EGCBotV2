package com.egc.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

import static com.egc.bot.Bot.*;

public class buttonManager extends ListenerAdapter {
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (Objects.equals(event.getButton().getId(), "acceptIcon")){
            Icon icon= null;
            try {
                icon = Icon.from(new File("icon.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Objects.requireNonNull(client.getGuildById(guildID)).getManager().setIcon(icon).queue();
            event.deferEdit().queue();
        }
        //System.out.println("Button Press: "+receiverID+": "+client.getGuildById(guildID).getMemberById(receiverID).getNickname());

        if (Objects.equals(event.getButton().getId(), "acceptTrade")&&event.getMember().getIdLong()==receiverID){
            System.out.println("Accept: "+receiverID+": "+client.getGuildById(guildID).getMemberById(receiverID).getNickname());
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle( "Trade Accepted.", null);
            eb.setColor(Color.red);
            //eb.setColor(new Color(0xF40C0C));
            //eb.setColor(new Color(255, 0, 54));
            MessageEmbed embed = eb.build();
            event.editMessageEmbeds(embed).setComponents().queue();
            try {
                inv.trade(traderID,receiverID,traderItem,receiverItem,traderCount,receiverCount);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else if(Objects.equals(event.getButton().getId(), "acceptTrade")&&event.getMember().getIdLong()!=receiverID){
            event.deferEdit().queue();
        }
        if (Objects.equals(event.getButton().getId(), "denyTrade")&&event.getMember().getIdLong()==receiverID){
            System.out.println("Deny: "+receiverID+": "+client.getGuildById(guildID).getMemberById(receiverID).getNickname());

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle( "Trade Denied.", null);
            eb.setColor(Color.red);
            //eb.setColor(new Color(0xF40C0C));
            //eb.setColor(new Color(255, 0, 54));
            MessageEmbed embed = eb.build();
            event.editMessageEmbeds(embed).setComponents().queue();
        }else if(Objects.equals(event.getButton().getId(), "denyTrade")&&event.getMember().getIdLong()!=receiverID){
            event.deferEdit().queue();
        }

        if (Objects.equals(event.getButton().getId(), "hit")&&event.getMember().getIdLong()==blackjackID){
            event.deferEdit().queue();
            bj.hit(event.getMessage().getIdLong(),event.getChannelIdLong());

        }else if(Objects.equals(event.getButton().getId(), "hit")&&event.getMember().getIdLong()!=blackjackID){
            event.deferEdit().queue();
        }
        if (Objects.equals(event.getButton().getId(), "stand")&&event.getMember().getIdLong()==blackjackID) {
            try {
                event.deferEdit().queue();
                bj.stand(event.getMessage().getIdLong(),event.getChannelIdLong());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else if(Objects.equals(event.getButton().getId(), "stand")&&event.getMember().getIdLong()!=blackjackID){
            event.deferEdit().queue();
        }

    }

}
