package com.egc.bot.commands;

import com.egc.bot.audio.GuildMusicManager;
import com.egc.bot.audio.PlayerManager;
import com.egc.bot.audio.TrackScheduler;
import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.managers.AudioManager;

public class Stop implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        Member member = ctx.getMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if(!memberVoiceState.inAudioChannel()) {
            ctx.reply("You need to be in a voice channel").queue();
            return;
        }

        Member self = ctx.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if(!selfVoiceState.inAudioChannel()) {
            ctx.reply("I am not in an audio channel").queue();
            return;
        }

        if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            ctx.reply("You are not in the same channel as me").queue();
            return;
        }
        AudioManager audioManager=ctx.getGuild().getAudioManager();
        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(ctx.getGuild());
        TrackScheduler trackScheduler = guildMusicManager.getTrackScheduler();
        trackScheduler.getQueue().clear();
        trackScheduler.getPlayer().stopTrack();
        ctx.reply("Stopped").queue();
        trackScheduler.getPlayer().destroy();
        audioManager.closeAudioConnection();

    }
}
