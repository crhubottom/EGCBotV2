package com.egc.bot.commands;

import com.egc.bot.audio.GuildMusicManager;
import com.egc.bot.audio.PlayerManager;
import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.database.gameDB;
import com.egc.bot.events.rocketEvent;
import com.egc.bot.events.tipEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.egc.bot.Bot.*;

public class recordAudio implements ICommand {
public static User u;



    public void run(SlashCommandInteraction ctx) throws IOException, InterruptedException {
        ctx.deferReply().queue();
        u=ctx.getUser();
        Member member = ctx.getMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!memberVoiceState.inAudioChannel()) {
            ctx.getHook().sendMessage("You need to be in a voice channel").queue();
            return;
        }
        Member self = ctx.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();
        if (!selfVoiceState.inAudioChannel()) {
            ctx.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
        } else {
            if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                ctx.getHook().sendMessage("You need to be in the same channel as me").queue();
                return;
            }
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Voice Request");
        eb.setDescription("Ready to record.");
        eb.setColor(Color.blue);
        ctx.getHook().sendMessageEmbeds(eb.build()).addActionRow(
                net.dv8tion.jda.api.interactions.components.buttons.Button.success("record", "Start Recording")).queue();

    }
}
