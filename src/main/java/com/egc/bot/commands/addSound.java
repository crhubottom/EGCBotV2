package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.io.File;

public class addSound implements ICommand {
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();

        Message.Attachment attachment = ctx.getOption("file").getAsAttachment();
        File folder = new File("/app/audioFiles"); // use container path

        // Create folder if it doesn't exist
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Get custom name
        String name = ctx.getOption("name").getAsString().trim();

        // Optional: sanitize name
        name = name.replaceAll("[^a-zA-Z0-9-_]", "_");

        // 🔹 CHECK FOR DUPLICATE NAME (ignoring extension)
        File[] files = folder.listFiles();
        if (files != null) {
            for (File existing : files) {
                if (!existing.isFile()) continue;

                String existingName = existing.getName();
                int dotIndex = existingName.lastIndexOf('.');
                String baseName = (dotIndex > 0)
                        ? existingName.substring(0, dotIndex)
                        : existingName;

                if (baseName.equalsIgnoreCase(name)) {
                    ctx.getHook().sendMessage("A sound with that name already exists.").queue();
                    return;
                }
            }
        }

        // Keep original extension
        String extension = attachment.getFileExtension();
        if (extension == null) extension = "dat";

        String fileName = name + "." + extension;

        // Full file path
        File file = new File(folder, fileName);

        // Download
        attachment.getProxy().downloadToFile(file)
                .thenAccept(f -> {
                    ctx.getHook().sendMessage("Saved to: " + f.getAbsolutePath()).queue();
                })
                .exceptionally(error -> {
                    ctx.getHook().sendMessage("Failed to save file").queue();
                    error.printStackTrace();
                    return null;
                });
    }
}