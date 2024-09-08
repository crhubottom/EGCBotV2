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
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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
        boolean audio=true;
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
        recievedBytes.clear();
            t.run(); //need to rename, records voice comms
        recievedBytes.clear();

        try {
            String voice=AIc.voiceToText("converted");
            //String out = AIc.gptCall(voice,"gpt-4o-mini");
            //String out="test";
           String out = AIc.gptCallWithSystem(voice,"You are transcribing voice audio. Your name is EGCBot, a friendly discord bot. " +
                   "Say \"play \"+song_name if the user is requesting a song to be played. " +
                   "Say \"tip\" if the user is requesting a game tip. " +
                   "Say \"spacex\" if the user is asking what the next SpaceX launch is." +
                   "Say \"rocket\" if the user is asking what the next rocket launch (in general) is." +
                   "Say \"rocket_LSP \"+LSP if the user is asking what the next rocket launch from an LSP that is not SpaceX. Do not give the LSP in acronyms, use the full name." +
                   "Say \"major_order\" if the user is asking what the Helldivers major order is." +
                   "Say \"top_gold\" if the user is asking who has the most gold." +
                   "Say \"my_gold\" if the user is asking how much gold they have." +
                   "Say \"top_game\" if the user is asking what the top played game is." +


                   "Respond normally for anything else.","gpt-4o-mini");
            System.out.println(out);
            if(out.startsWith("play ")){
                String name=out.substring(4);
                out="Playing "+name;
                try {
                    new URL(name);
                } catch (MalformedURLException e) {
                    name = "ytsearch:" + name;
                }
                PlayerManager playerManager = PlayerManager.get();
                playerManager.play(ctx.getGuild(), name);
                audio=false;

            }else if(out.startsWith("rocket_LSP ")){
                String lsp=out.substring(11);
                System.out.println(lsp);
                out=rocketEvent.nextLaunchWithLSP(lsp).toString();
            }
            switch(out){
                case "tip":
                    tipEvent tipE=new tipEvent();
                    out=tipE.tip();
                    audio=false;
                    break;
                    case "spacex":
                        out= rocketEvent.nextLaunch(false,true).toString();
                        break;
                        case "rocket":
                            out= rocketEvent.nextLaunch(true,true).toString();
                            break;
                            case "major_order":
                                String orderDesc=null;
                                JSONArray jsonArray  = new JSONArray(IOUtils.toString(new URL("https://helldiverstrainingmanual.com/api/v1/war/major-orders"), StandardCharsets.UTF_8));
                                String j=jsonArray.toString();
                                for (int i = 0; i < jsonArray.length(); ++i) {
                                    JSONObject rec = jsonArray.getJSONObject(i);
                                    JSONObject setting = rec.getJSONObject("setting");
                                    orderDesc = setting.getString("overrideBrief");
                                }
                                out="The current HellDivers Major Order is "+orderDesc;
                                break;
                                case "top_gold":
                                    out="The user with the most gold is "+inv.topGold();
                                    break;
                                case "my_gold":
                                    out="You have "+inv.getGold(ctx.getMember().getIdLong())+" gold.";
                                     break;
                                    case "top_game":
                                        out="The most played game is "+ gameDB.topGame();
                                        break;
                default:
                    break;

            }
            if(audio) {
                try {

                    AIc.ttsCall(out, "output");
                    PlayerManager playerManager = PlayerManager.get();
                    playerManager.play(ctx.getGuild(), "output.mp3");
                    ctx.getHook().sendMessage(voice).queue();

                } catch (Exception e) {
                    ctx.getHook().sendMessage(e.toString()).queue();
                }
            }else {
                ctx.getHook().sendMessage(out).queue();
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
