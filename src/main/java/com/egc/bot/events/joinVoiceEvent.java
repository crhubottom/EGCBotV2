package com.egc.bot.events;

import com.egc.bot.AIController;
import com.egc.bot.audio.PlayerManager;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static com.egc.bot.Bot.*;

public class joinVoiceEvent extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        // Detect a user joining a voice channel for the first time
        if (event.getChannelJoined() != null && event.getChannelLeft() == null) {
            System.out.println(event.getMember().getEffectiveName() +
                    " joined " + event.getChannelJoined().getName());
            if(event.getMember().getId().equals(keys.get("BOT_ID"))){
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
            List<String> responses = List.of(
                    "Welcome to the fucking voice channel " + event.getMember().getEffectiveName() + ".",
                    "Hey " + event.getMember().getEffectiveName() + ", good to fucking see you!",
                    "Whats up " + event.getMember().getEffectiveName() + "?",
                    event.getMember().getEffectiveName() + " finally fucking joined!",
                    "A wild " + event.getMember().getEffectiveName() + " appears!"
            );
            try {
                AIc.ttsCall( responses.get(new Random().nextInt(responses.size())), "welcome");
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