package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.io.File;

public class sounds implements ICommand {

    @Override
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();

        File folder = new File("/app/audioFiles");

        // Check if folder exists
        if (!folder.exists() || !folder.isDirectory()) {
            ctx.getHook().sendMessage("Audio folder not found at /app/audioFiles").queue();
            return;
        }

        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            ctx.getHook().sendMessage("No sounds found.").queue();
            return;
        }

        StringBuilder response = new StringBuilder("Available sounds:\n");

        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();

                // Remove extension
                int dotIndex = name.lastIndexOf('.');
                if (dotIndex > 0) {
                    name = name.substring(0, dotIndex);
                }

                response.append("- ").append(name).append("\n");
            }
        }

        ctx.getHook().sendMessage(response.toString()).queue();
    }
}
