package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import java.awt.Color;

import static com.egc.bot.Bot.*;

public class listItem implements ICommand {
    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        String username = ctx.getMember().getNickname();
        long id=ctx.getMember().getIdLong();
        if(ctx.getOption("user")!=null&&!ctx.getOption("user").getAsString().isEmpty()) {
            if (!client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).isEmpty()) {
                username = client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).get(0).getNickname();
                id=client.getGuildById(guildID).getMembersByNickname(ctx.getOption("user").getAsString(), true).get(0).getIdLong();
            }else{
                ctx.getHook().sendMessage("User not found.").queue();
                return;
            }
        }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(username+ "'s Inventory", null);
            eb.setColor(Color.red);
            eb.setColor(new Color(0xF40C0C));
            eb.setColor(new Color(255, 0, 54));
            eb.setDescription(inv.listInv(id));
            ctx.getHook().sendMessageEmbeds(eb.build()).queue();

    }


}