package com.egc.bot.events;

import com.egc.bot.AIController;
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
        List<String> possibleOutput = new ArrayList<>();
        List<String> responses = List.of(
                "Welcome to the fucking voice channel " + event.getMember().getEffectiveName() + ".",
                "Hey " + event.getMember().getEffectiveName() + ", good to fucking see you!",
                "Whats up " + event.getMember().getEffectiveName() + "?",
                event.getMember().getEffectiveName() + " finally fucking joined!",
                "A wild " + event.getMember().getEffectiveName() + " appears!"
        );
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
                    member=content.substring(content.indexOf("{")+1,content.indexOf("}"));
                    System.out.println("Member is "+member);
                    if(member.equals(event.getMember().getEffectiveName())){
                        output.append(content, 0, content.indexOf("^"));
                        output.append(event.getMember().getEffectiveName());
                        output.append(content, content.indexOf("^")+1, content.indexOf("{"));
                        possibleOutput.add(output.toString());
                    }
                    }else {
                        output.append(content, 0, content.indexOf("^"));
                        output.append(event.getMember().getEffectiveName());
                        output.append(content.substring(content.indexOf("^")+1));
                        possibleOutput.add(output.toString());
                    }
                    System.out.println(output);



                }



            }
            try {
                AIc.ttsCall(possibleOutput.get(new Random().nextInt(possibleOutput.size())), "welcome");
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
