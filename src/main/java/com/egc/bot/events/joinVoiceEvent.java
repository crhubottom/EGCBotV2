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
        TextChannel tc = null;
        String content = null;
        String member = null;
        List<Message> messages;
        StringBuilder output;
        List<String> possibleOutputGeneral = new ArrayList<>();
        List<String> possibleOutputPersonalized = new ArrayList<>();
        if (event.getChannelJoined() != null && event.getChannelLeft() == null) {
            System.out.println(event.getMember().getEffectiveName() +
                    " joined " + event.getChannelJoined().getName());
            if (event.getMember().getId().equals(keys.get("BOT_ID"))) {
                try {
                    AIc.ttsCall("Whats up fuckers!", "welcome");
                    PlayerManager playerManager = PlayerManager.get();
                    playerManager.play(client.getGuildById(guildID), "welcome.mp3");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            tc = event.getGuild().getTextChannelById("1491968908805279844");
            // Get latest message
            Message latestMessage = tc.retrieveMessageById(tc.getLatestMessageId()).complete();
            MessageHistory history = tc.getHistoryBefore(tc.getLatestMessageId(), 100).complete();
            messages = new ArrayList<>();
            messages.add(latestMessage); // add newest first
            messages.addAll(history.getRetrievedHistory());

            for (Message m : messages) {
                output = new StringBuilder();
                content=m.getContentRaw();
                //System.out.println(m.getContentRaw());

                if(content.contains("^")){
                    if(content.contains("{")&&content.contains("}")){
                        if(content.substring(content.indexOf("{")+1,content.indexOf("}")).contains(event.getMember().getEffectiveName())){
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
                    //System.out.println(output);



                }



            }
            try {
                if(!possibleOutputPersonalized.isEmpty()){
                    int rand = new Random().nextInt(2);
                    System.out.println(rand);
                    if(rand==0){
                        for (String s:possibleOutputPersonalized){
                            System.out.println(s);
                        }
                        AIc.ttsCall(possibleOutputGeneral.get(new Random().nextInt(possibleOutputGeneral.size())), "welcome");
                    }else{
                        for (String s:possibleOutputGeneral){
                            System.out.println(s);
                        }
                        AIc.ttsCall(possibleOutputPersonalized.get(new Random().nextInt(possibleOutputPersonalized.size())), "welcome");
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
