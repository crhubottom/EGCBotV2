package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import io.github.stefanbratanov.jvm.openai.OpenAIException;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.egc.bot.Bot.AIc;

public class changeIcon implements ICommand {

    public void run(SlashCommandInteraction ctx) throws IOException {
        ctx.deferReply().queue();
        String prompt;
        try {
            if(ctx.getOption("style")==null||ctx.getOption("style").getAsString()==null||ctx.getOption("style").getAsString().equals("")){
               prompt="anything";
            }else{
                prompt= Objects.requireNonNull(ctx.getOption("style")).getAsString();
            }
            System.out.println(prompt);
            AIc.dalleCall("A 2d circular icon for a discord server with the word \"EGC\" in the center. In the style of: "+prompt+".","icon");
            java.io.File a = new File("icon.png");
            BufferedImage bufferedImage = cropImage(a, 90, 90, 870, 860);
            File outputfile = new File("iconCropped.png");
            ImageIO.write(bufferedImage, "png", outputfile);
            FileUpload upload = FileUpload.fromData(outputfile, "iconCropped.png");
            ctx.getHook().sendFiles(upload).addActionRow(
                    Button.success("acceptIcon", "Set Icon")).queue();
        } catch (OpenAIException e) {
            ctx.getHook().sendMessage(e.toString()).queue();
        }
    }
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
}
