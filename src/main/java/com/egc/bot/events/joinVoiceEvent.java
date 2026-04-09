package com.egc.bot.events;

import com.egc.bot.AIController;
import com.egc.bot.audio.PlayerManager;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

import static com.egc.bot.Bot.*;

public class joinVoiceEvent extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        // Detect a user joining a voice channel for the first time
        if (event.getChannelJoined() != null && event.getChannelLeft() == null) {
            System.out.println(event.getMember().getEffectiveName() +
                    " joined " + event.getChannelJoined().getName());

            String response = AIc.gptCallWithSystem(
                    "Give a very short, one-line greeting to the user " + event.getMember().getEffectiveName() + " who just joined the voice channel.",
                    "Use swear words to welcome the user to the voice channel. No emojis. Less than 20 words",
                    textModel
            );            try {
                AIc.ttsCall(response, "welcome");
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