package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


/**
 * Basic Ping command
 */
public class helldiversMajorOrder implements ICommand {
    String orderDesc=null;
    int totalSecs = 0;
    String timeString = null;
    public void run(SlashCommandInteraction ctx) throws IOException {
        ctx.deferReply().queue();
        JSONArray jsonArray  = new JSONArray(IOUtils.toString(new URL("https://helldiverstrainingmanual.com/api/v1/war/major-orders"), StandardCharsets.UTF_8));
        String j=jsonArray.toString();
        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject rec = jsonArray.getJSONObject(i);
            JSONObject setting = rec.getJSONObject("setting");
            JSONArray tasks = setting.getJSONArray("tasks");
            JSONObject value = tasks.getJSONObject(0);
            orderDesc = setting.getString("overrideBrief");
             totalSecs = rec.getInt("expiresIn");
            int day = (int)TimeUnit.SECONDS.toDays(totalSecs);
            long hours = TimeUnit.SECONDS.toHours(totalSecs) - (day * 24L);
            long minute = TimeUnit.SECONDS.toMinutes(totalSecs) - (TimeUnit.SECONDS.toHours(totalSecs)* 60);
            long second = TimeUnit.SECONDS.toSeconds(totalSecs) - (TimeUnit.SECONDS.toMinutes(totalSecs) *60);
            timeString = String.format("%02d:%02d:%02d:%02d", day, hours, minute, second);
        }
        ctx.getHook().sendMessage("**MAJOR ORDER:** "+orderDesc+"\n**Time Left:** "+timeString).queue();
    }
}
