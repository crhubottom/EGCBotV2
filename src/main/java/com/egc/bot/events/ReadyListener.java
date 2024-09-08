package com.egc.bot.events;

import com.egc.bot.Bot;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Prints out when bot is ready
 */
public class ReadyListener extends ListenerAdapter {
    
    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Logged in as: " + Bot.client.getSelfUser().getName());
    }
}
