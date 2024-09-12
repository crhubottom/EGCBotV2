package com.egc.bot;

import com.egc.bot.audio.PlayerManager;
import com.egc.bot.database.gameDB;
import com.egc.bot.events.rocketEvent;
import com.egc.bot.events.tipEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Objects;

import static com.egc.bot.Bot.*;

public class buttonManager extends ListenerAdapter {
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (Objects.equals(event.getButton().getId(), "acceptIcon")){
            Icon icon= null;
            try {
                icon = Icon.from(new File("iconCropped.png"));
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
        if (Objects.equals(event.getButton().getId(), "record")) {
            boolean audio=true;
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle( "Voice Request", null);
            eb.setColor(Color.red);
            eb.setDescription("Recording...");
            MessageEmbed embed = eb.build();
            event.editMessageEmbeds(embed).setComponents().setActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.success("endRecording", "Stop Recording")).queue();
            recievedBytes.clear();
            try {
                t.run(); //need to rename, records voice comms
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            recievedBytes.clear();

            try {
                String voice=AIc.voiceToText("converted");
                //String out = AIc.gptCall(voice,"gpt-4o-mini");
                //String out="test";
                String out = AIc.gptCallWithSystem(voice,"You are transcribing voice audio. Your name is EGCBot, a friendly discord bot. " +
                        "Say \"play \"+song_name if the user is requesting a song to be played. " +
                        "Say \"tip\" if the user is requesting a game tip. " +
                        "Say \"spacex\" if the user is asking what the next SpaceX launch is." +
                        "Say \"rocket\" if the user is asking what the next rocket launch (in general) is." +
                        "Say \"rocket_LSP \"+LSP if the user is asking what the next rocket launch from an LSP that is not SpaceX. Do not give the LSP in acronyms, use the full name." +
                        "Say \"major_order\" if the user is asking what the Helldivers major order is." +
                        "Say \"top_gold\" if the user is asking who has the most gold." +
                        "Say \"my_gold\" if the user is asking how much gold they have." +
                        "Say \"top_game\" if the user is asking what the top played game is." +


                        "Respond normally for anything else.","gpt-4o-mini");
                System.out.println(out);
                if(out.startsWith("play ")){
                    String name=out.substring(4);
                    out="Playing "+name;
                    try {
                        new URL(name);
                    } catch (MalformedURLException e) {
                        name = "ytsearch:" + name;
                    }
                    PlayerManager playerManager = PlayerManager.get();
                    playerManager.play(event.getGuild(), name);
                    audio=false;

                }else if(out.startsWith("rocket_LSP ")){
                    String lsp=out.substring(11);
                    System.out.println(lsp);
                    out= rocketEvent.nextLaunchWithLSP(lsp).toString();
                }
                switch(out){
                    case "tip":
                        tipEvent tipE=new tipEvent();
                        out=tipE.tip();
                        audio=false;
                        break;
                    case "spacex":
                        out= rocketEvent.nextLaunch(false,true).toString();
                        break;
                    case "rocket":
                        out= rocketEvent.nextLaunch(true,true).toString();
                        break;
                    case "major_order":
                        String orderDesc=null;
                        JSONArray jsonArray  = new JSONArray(IOUtils.toString(new URL("https://helldiverstrainingmanual.com/api/v1/war/major-orders"), StandardCharsets.UTF_8));
                        String j=jsonArray.toString();
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject rec = jsonArray.getJSONObject(i);
                            JSONObject setting = rec.getJSONObject("setting");
                            orderDesc = setting.getString("overrideBrief");
                        }
                        out="The current HellDivers Major Order is "+orderDesc;
                        break;
                    case "top_gold":
                        out="The user with the most gold is "+inv.topGold();
                        break;
                    case "my_gold":
                        out="You have "+inv.getGold(event.getMember().getIdLong())+" gold.";
                        break;
                    case "top_game":
                        out="The most played game is "+ gameDB.topGame();
                        break;
                    default:
                        break;

                }
                if(audio) {
                    try {

                        AIc.ttsCall(out, "output");
                        PlayerManager playerManager = PlayerManager.get();
                        playerManager.play(event.getGuild(), "output.mp3");
                    } catch (Exception e) {
                       System.out.println(e);
                    }
                }


            }catch (Exception e){
                System.out.println(e);
            }

        }
        if(Objects.equals(event.getButton().getId(), "endRecording")){
            record=false;
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Voice Request");
            eb.setDescription("Ready to record.");
            eb.setColor(Color.blue);
            MessageEmbed embed = eb.build();
            event.editMessageEmbeds(embed).setComponents().setActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.success("record", "Start Recording")).queue();

        }

    }

}
