package com.egc.bot.commands;

import com.egc.bot.audio.PlayerManager;
import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class play implements ICommand {
    public void run(SlashCommandInteraction ctx) throws Exception {
        Member member = ctx.getMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if(!memberVoiceState.inAudioChannel()) {
            ctx.reply("You need to be in a voice channel").queue();
            return;
        }

        Member self = ctx.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if(!selfVoiceState.inAudioChannel()) {
            ctx.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
        } else {
            if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                ctx.reply("You need to be in the same channel as me").queue();
                return;
            }
        }

        String name = ctx.getOption("name").getAsString();

        try {
            new URL(name);
        } catch (MalformedURLException e) {
            name = "ytsearch:" + name;
        }
        PlayerManager playerManager = PlayerManager.get();
        playerManager.play(ctx.getGuild(), name);
        ctx.reply("Playing").queue();
    }
}