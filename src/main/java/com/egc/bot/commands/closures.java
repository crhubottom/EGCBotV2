package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;


public class closures implements ICommand {

    public void run(SlashCommandInteraction ctx) throws IOException {
        ctx.deferReply().queue();
        Calendar cal = Calendar.getInstance();
        String date = new SimpleDateFormat("MMMM dd, yyyy").format(cal.getTime());
        JSONArray jsonArray  = new JSONArray(IOUtils.toString(new URL("https://starbase.nerdpg.live/api/json/roadClosures"), StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if(date.equals(jsonObject.getString("date"))){
                if(Objects.equals(jsonObject.getString("time"), "12:00 am to 2:00 pm")){
                    builder.append("**").append(jsonObject.getString("type")).append(": ").append(jsonObject.getString("date")).append("  ").append(jsonObject.getString("time")).append("     LAUNCH CLOSURE**\n");
                }else if(Objects.equals(jsonObject.getString("time"), "5:00 am to 5:00 pm")){
                    builder.append(jsonObject.getString("type")).append(": ").append(jsonObject.getString("date")).append("  ").append(jsonObject.getString("time")).append("     WDR CLOSURE\n");
                }else{
                    builder.append("**").append(jsonObject.getString("type")).append(": ").append(jsonObject.getString("date")).append("  ").append(jsonObject.getString("time")).append("**\n");

                }
            }else {
                if (Objects.equals(jsonObject.getString("time"), "12:00 am to 2:00 pm")) {
                    builder.append(jsonObject.getString("type")).append(": ").append(jsonObject.getString("date")).append("  ").append(jsonObject.getString("time")).append("     LAUNCH CLOSURE\n");

                }else if(Objects.equals(jsonObject.getString("time"), "5:00 am to 5:00 pm")){
                    builder.append(jsonObject.getString("type")).append(": ").append(jsonObject.getString("date")).append("  ").append(jsonObject.getString("time")).append("     WDR CLOSURE\n");

                }else{
                    builder.append(jsonObject.getString("type")).append(": ").append(jsonObject.getString("date")).append("  ").append(jsonObject.getString("time")).append("\n");
                }
            }

        }
       // System.out.println(builder);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.blue);
        eb.setTitle("Starbase Road Closures");
        eb.setDescription(builder.toString());
        ctx.getHook().sendMessageEmbeds(eb.build()).queue();
    }
}
