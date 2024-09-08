package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.events.rocketEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

import static com.egc.bot.events.rocketEvent.*;

public class nextLaunch implements ICommand {

    public void run(SlashCommandInteraction ctx) throws IOException, InterruptedException, SQLException {
        ctx.deferReply().queue();
        String out;
    if (ctx.getOption("spacex") == null) {
        out=rocketEvent.nextLaunch(true,false).toString();
        ctx.getHook().sendMessageEmbeds(new EmbedBuilder().setColor(Color.orange).setTitle(tminus).setDescription(out).setImage("attachment://rocket"+rcktFormat).build()) .addFiles(FileUpload.fromData(upload)).queue();

    } else if (Objects.requireNonNull(ctx.getOption("spacex")).getAsString().equalsIgnoreCase("f")) {
        out=rocketEvent.nextLaunch(true,false).toString();
        ctx.getHook().sendMessageEmbeds(new EmbedBuilder().setColor(Color.orange).setTitle(tminus).setDescription(out).setImage("attachment://rocket"+rcktFormat).build()) .addFiles(FileUpload.fromData(upload)).queue();
    } else {
        out=rocketEvent.nextLaunch(false,false).toString();
        ctx.getHook().sendMessageEmbeds(new EmbedBuilder().setColor(Color.orange).setTitle(tminus).setDescription(out).setImage("attachment://rocket"+rcktFormat).build()) .addFiles(FileUpload.fromData(upload)).queue();
    }

    }
}
