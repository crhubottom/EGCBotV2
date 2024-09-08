package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;


public class meme implements ICommand {
ArrayList<String> memes= new ArrayList<>();
    public void run(SlashCommandInteraction ctx) throws IOException {
        ctx.deferReply().queue();
        String format=".gif";
        while(format.equals(".gif")){
        JSONObject jsonObject  = new JSONObject(IOUtils.toString(new URL("https://meme-api.com/gimme"), StandardCharsets.UTF_8));
        String j=jsonObject.toString();
        String link=jsonObject.getString("url");
         format=link.substring(link.lastIndexOf("."));
            System.out.println("\n");
            for (String meme : memes) {
                System.out.println(meme);
            }
    if(memes.contains(link)){
        format=".gif";
        System.out.println("duplicate");
    }else if(!format.equals(".gif")){
        memes.add(link);
    }
            if(!format.equals(".gif")) {
        String sub=jsonObject.getString("subreddit");
            String title=jsonObject.getString("title");
        try(InputStream in = new URL(link).openStream()){

            Files.copy(in, Paths.get("meme"+format), StandardCopyOption.REPLACE_EXISTING);
        }
        File a=new File("meme"+format);
        FileUpload upload = FileUpload.fromData(a, "image.png");

                ctx.getHook().sendMessage("From " + sub + "\n" + title).addFiles(upload).queue();
            }
    }
    }
}
