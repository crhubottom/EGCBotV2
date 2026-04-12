package com.egc.bot.commands;

import com.egc.bot.audio.PlayerManager;
import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.io.File;

public class playSound implements ICommand {

    @Override
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();

        String soundName = ctx.getOption("name").getAsString();
        File folder = new File("/app/audioFiles");

        if (!folder.exists() || !folder.isDirectory()) {
            ctx.getHook().sendMessage("Audio folder not found.").queue();
            return;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            ctx.getHook().sendMessage("No sounds found.").queue();
            return;
        }

        File matchedFile = null;

        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }

            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            String baseName = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;

            if (baseName.equalsIgnoreCase(soundName)) {
                matchedFile = file;
                break;
            }
        }

        if (matchedFile == null) {
            ctx.getHook().sendMessage("Sound not found: " + soundName).queue();
            return;
        }

        PlayerManager playerManager = PlayerManager.get();
        playerManager.play(ctx.getGuild(), matchedFile.getAbsolutePath());

        ctx.getHook().sendMessage("Playing: " + soundName).queue();
    }
}
