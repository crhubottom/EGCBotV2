package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static com.egc.bot.respond.dnd;
import static com.egc.bot.respond.story;

public class toggleDnD implements ICommand {
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        try {
            dnd= !dnd;
            if(!dnd){
                FileUtils.writeStringToFile(new File("story.txt"), story.toString(), StandardCharsets.UTF_8);
                java.io.File output=new File("story.txt");
                FileUpload upload = FileUpload.fromData(output, "story.txt");
                ctx.getChannel().sendMessage("Full story").addFiles(upload).queue();
                story.delete(0, story.length());

                if(output.delete()){
                    System.out.println("Deleted story");
                }else {
                    System.out.println("Failed to delete story");
                }
            }
            ctx.getHook().sendMessage("DnD mode set to "+dnd).queue();
        }catch (Exception e){
            ctx.getHook().sendMessage(e.toString()).queue();
        }
    }

}