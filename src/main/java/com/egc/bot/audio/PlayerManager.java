package com.egc.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.*;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

import static com.egc.bot.Bot.keys;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

    private PlayerManager() {
        YoutubeSourceOptions options = new YoutubeSourceOptions().setRemoteCipher("https://cipher.kikkia.dev/","","");
        options.setAllowSearch(true);
        //dev.lavalink.youtube.YoutubeAudioSourceManager youtube = new dev.lavalink.youtube.YoutubeAudioSourceManager(true, new TvHtml5Embedded(),new Tv(), new WebEmbedded());
        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(options,new TvHtml5Embedded(),new Tv(), new WebEmbedded());
        youtube.useOauth2(keys.get("Oauth"),true);
        this.audioPlayerManager.registerSourceManager(youtube);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);


    }

    public static PlayerManager get() {
        if(INSTANCE == null) {
            INSTANCE = new PlayerManager();

        }
        return INSTANCE;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return guildMusicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            GuildMusicManager musicManager = new GuildMusicManager(audioPlayerManager, guild);

            guild.getAudioManager().setSendingHandler(musicManager.getAudioForwarder());

            return musicManager;
        });
    }

    public void play(Guild guild, String trackURL) {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        System.out.println(trackURL);
        System.out.println(guild.getName());
        audioPlayerManager.loadItemOrdered(guildMusicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                guildMusicManager.getTrackScheduler().queue(track);

                System.out.println("queued track");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                guildMusicManager.getTrackScheduler().queue(playlist.getTracks().get(0));
                System.out.println("queued playlist");
            }

            @Override
            public void noMatches() {
                System.out.println("No matches");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("Failed to load"+exception.toString());
            }
        });
    }
}
