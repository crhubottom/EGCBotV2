package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import io.github.stefanbratanov.jvm.openai.OpenAIException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.egc.bot.Bot.AIc;

public class dalleCall implements ICommand {

    private static final int DISCORD_MESSAGE_LIMIT = 2000;

    @Override
    public void run(SlashCommandInteraction ctx) {
        // Validate the option before deferring so we can reply ephemerally
        OptionMapping promptOption = ctx.getOption("prompt");
        if (promptOption == null || promptOption.getAsString().isBlank()) {
            ctx.reply("Please provide a prompt.").setEphemeral(true).queue();
            return;
        }
        String prompt = promptOption.getAsString();

        ctx.deferReply().queue();

        // Run the slow API call off the JDA event thread
        CompletableFuture.runAsync(() -> {
            // Unique name per request so concurrent commands don't overwrite each other
            String baseName = "image-" + UUID.randomUUID();
            File imageFile = new File(baseName + ".png");

            try {
                AIc.dalleCall(prompt, baseName);

                if (!imageFile.exists()) {
                    sendError(ctx, "Image generation failed: no image was produced.");
                    return;
                }

                ctx.getHook()
                        .sendFiles(FileUpload.fromData(imageFile, "image.png"))
                        .queue(
                                success -> imageFile.delete(),
                                failure -> {
                                    imageFile.delete();
                                    sendError(ctx, "Failed to upload image: " + failure.getMessage());
                                });
            } catch (OpenAIException e) {
                imageFile.delete();
                sendError(ctx, "OpenAI error: " + e.getMessage());
            } catch (Exception e) {
                imageFile.delete();
                sendError(ctx, "Something went wrong: " + e.getMessage());
            }
        });
    }

    private static void sendError(SlashCommandInteraction ctx, String message) {
        if (message.length() > DISCORD_MESSAGE_LIMIT) {
            message = message.substring(0, DISCORD_MESSAGE_LIMIT - 3) + "...";
        }
        ctx.getHook().sendMessage(message).queue();
    }
}