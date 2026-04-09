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

           String response= AIc.gptCallWithSystem("Very short greeting to the user"+event.getMember().getEffectiveName()+" who just joined the voice channel. Do not mention the name of the channel. Just greet the user.", "You can be funny, rude, mean, or normal. You can say cuss words and things like \"Welcome to the fucking channel david\".", textModel);
            try {
                AIc.ttsCall(response, "welcome");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }


    }
}