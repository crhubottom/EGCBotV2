package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.egc.bot.Bot.keys;

/**
 * Basic Ping command
 */
public class joke implements ICommand {

    public void run(SlashCommandInteraction ctx) throws IOException {
        ctx.deferReply().queue();
        String apiKey = keys.get("joke");
        URL url = new URL("https://api.api-ninjas.com/v1/jokes?limit=1");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("X-Api-Key",apiKey);
        InputStream responseStream = connection.getInputStream();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseStream);
        String rootText=root.toString();
        System.out.println(rootText);
        rootText=rootText.substring(rootText.indexOf("\":\"")+3,rootText.indexOf("}")-1);
        System.out.println("root: "+rootText);
        ctx.getHook().sendMessage(rootText).queue();
    }
}
