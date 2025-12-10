package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import io.github.stefanbratanov.jvm.openai.OpenAIException;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.egc.bot.Bot.AIc;

public class changeIcon implements ICommand {

    public void run(SlashCommandInteraction ctx) throws IOException {
        ctx.deferReply().queue();
        String prompt;
        if (ctx.getOption("style") == null || ctx.getOption("style").getAsString().isEmpty()) {
            prompt = "anything";
        } else {
            prompt = Objects.requireNonNull(ctx.getOption("style")).getAsString();
        }
        String finalPrompt=prompt;
        CompletableFuture.runAsync(() -> {
            try {
                // Call DALL·E (or GPT-image-1) to generate the image
                AIc.dalleCall("A 2d circular icon for a discord server with the word EGC in the center. In the style of: " + finalPrompt + ".", "icon");

                File imageFile = new File("icon.png");

                // Optional: Wait for file to exist (max 10 seconds)
                if (!imageFile.exists() || imageFile.length() == 0) {
                    ctx.getHook().sendMessage("Image generation took too long or failed.").queue();
                    return;
                }

                FileUpload upload = FileUpload.fromData(imageFile, "icon.png");
                ctx.getHook()
                        .sendFiles(upload)
                        .setComponents(
                                ActionRow.of(
                                        Button.success("acceptIcon", "Set Icon")
                                )
                        )
                        .queue();


            } catch (Exception e) {
                e.printStackTrace();
                ctx.getHook().sendMessage("Failed to generate image: " + e.getMessage()).queue();
            }
        });
    }
    /*
    private BufferedImage cropImage(File filePath, int x, int y, int w, int h){
        try {
            BufferedImage originalImgage = ImageIO.read(filePath);

            BufferedImage subImgage = originalImgage.getSubimage(x, y, w, h);
            return subImgage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

     */
}
