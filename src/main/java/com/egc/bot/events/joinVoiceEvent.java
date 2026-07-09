package com.egc.bot.events;

import com.egc.bot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.egc.bot.Bot.*;

public class joinVoiceEvent extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        // Detect a user joining a voice channel for the first time
        TextChannel tc;
        String content;
        List<Message> messages;
        StringBuilder output;
        List<String> possibleOutputGeneral = new ArrayList<>();
        List<String> possibleOutputBot = new ArrayList<>();
        List<String> possibleOutputPersonalized = new ArrayList<>();
        if (event.getChannelJoined() != null && event.getChannelLeft() == null) {
            System.out.println(event.getMember().getEffectiveName() + " joined " + event.getChannelJoined().getName());
            tc = event.getGuild().getTextChannelById("1491968908805279844");
            Message latestMessage = tc.retrieveMessageById(tc.getLatestMessageId()).complete();
            MessageHistory history = tc.getHistoryBefore(tc.getLatestMessageId(), 100).complete();
            messages = new ArrayList<>();
            messages.add(latestMessage); // add newest first
            messages.addAll(history.getRetrievedHistory());
            for (Message m : messages) {
                output = new StringBuilder();
                content=m.getContentRaw();
                String userInfo=content.substring(content.indexOf("{")+1,content.indexOf("}"));
                if(userInfo.contains("Bot")){
                    if(content.contains("^")) {
                        output.append(content, 0, content.indexOf("^"));
                        output.append(event.getMember().getEffectiveName());
                        output.append(content, content.indexOf("^") + 1, content.indexOf("{"));
                        possibleOutputBot.add(output.toString());
                    }else{
                        possibleOutputBot.add(content.substring(0,content.indexOf("{")));
                    }
                }else{
                    if(content.contains("^")){
                        if(content.contains("{")&&content.contains("}")){
                            if(userInfo.contains(event.getMember().getEffectiveName())){
                                output.append(content, 0, content.indexOf("^"));
                                output.append(event.getMember().getEffectiveName());
                                output.append(content, content.indexOf("^")+1, content.indexOf("{"));
                                possibleOutputPersonalized.add(output.toString());
                            }

                        }else {
                            output.append(content, 0, content.indexOf("^"));
                            output.append(event.getMember().getEffectiveName());
                            output.append(content.substring(content.indexOf("^")+1));
                            possibleOutputGeneral.add(output.toString());
                        }
                    }

                }
            }
            if (event.getMember().getId().equals(keys.get("BOT_ID"))) {
                try {
                    for (String s:possibleOutputBot){
                        System.out.println(s);
                    }
                    AIc.ttsCall(possibleOutputBot.get(rand.nextInt(possibleOutputBot.size())), "welcome");
                    PlayerManager playerManager = PlayerManager.get();
                    playerManager.play(client.getGuildById(guildID), "welcome.mp3");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            try {
                if(!possibleOutputPersonalized.isEmpty()){
                    int randNum = rand.nextInt(2);
                    System.out.println(randNum);
                    if(randNum==0){
                        for (String s:possibleOutputGeneral){
                            System.out.println(s);
                        }
                        AIc.ttsCall(possibleOutputGeneral.get(rand.nextInt(possibleOutputGeneral.size())), "welcome");
                    }else{
                        for (String s:possibleOutputPersonalized){
                            System.out.println(s);
                        }
                        AIc.ttsCall(possibleOutputPersonalized.get(rand.nextInt(possibleOutputPersonalized.size())), "welcome");
                    }
                }
                PlayerManager playerManager = PlayerManager.get();
                playerManager.play(client.getGuildById(guildID), "welcome.mp3");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
