package com.egc.bot.events;

import com.egc.bot.AIController;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

import static com.egc.bot.Bot.AIc;
import static com.egc.bot.Bot.textModel;

public class joinVoiceEvent extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        // Detect a user joining a voice channel for the first time
        if (event.getChannelJoined() != null && event.getChannelLeft() == null) {
            System.out.println(event.getMember().getEffectiveName() +
                    " joined " + event.getChannelJoined().getName());

            String response = AIc.gptCallWithSystem(
                    "Give a very short, one-line greeting to the user " + event.getMember().getEffectiveName() + " who just joined. Do not mention the channel.",
                    "Be witty, sarcastic, and playful. You may use mild swearing and light roast-style humor, and may insult the user.",
                    textModel
            );            try {
                AIc.ttsCall(response, "welcome");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }


    }
}