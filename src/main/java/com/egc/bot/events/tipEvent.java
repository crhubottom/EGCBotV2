package com.egc.bot.events;

import com.egc.bot.audio.PlayerManager;
import com.egc.bot.database.tipDB;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.egc.bot.Bot.*;


public class tipEvent {
    public String tip() throws IOException, InterruptedException {
        System.out.println("tip called");
        int topMembers = 0;
        VoiceChannel vcID = null;
        List<VoiceChannel> lis = new ArrayList<>();
        String guild = keys.get("GUILD");
        Member self = client.getGuildById(guild).getSelfMember();

        lis = client.getGuildById(guild).getVoiceChannels();
        for (VoiceChannel li : lis) {
            if (li.getMembers().size() > topMembers) {
                topMembers = li.getMembers().size();
                vcID = li;
            }
        }
        if (topMembers == 1) {
            System.out.println("no members in vc");
            return "There is no one in the vc";

        }

        int gameCount;
        String topGame;
        int playCount = 1;
        String tip;
        boolean tie=false;
        ArrayList<String> ties = new ArrayList<>();
        List<Member> a = client.getGuildById(guild).getVoiceChannelById(vcID.getId()).getMembers();
        ArrayList<String> ls = new ArrayList<>();
        StringBuilder players = new StringBuilder();
        for (Member member : a) {

            if(!member.getUser().isBot()) {
                System.out.println(member.getActivities());
                if (!member.getActivities().isEmpty()) {
                    String game = member.getActivities().get(0).toString();
                    if (game.contains("RichPresence:")) {
                        ls.add(game.substring(game.indexOf("RichPresence:") + 13, game.indexOf("(", game.indexOf("RichPresence:") + 13)));
                    }
                    if (game.contains("[PLAYING]:")) {
                        System.out.println(game);
                        System.out.println(game.substring(game.indexOf("[PLAYING]:") + 10));
                        ls.add(game.substring(game.indexOf("[PLAYING]:") + 10));
                    }
                }
            }
        }
        if (ls.isEmpty()) {
            ls.add("life");
            System.out.println("life");
        }
        topGame = ls.get(0);

        for (int y = 0; y < ls.size(); y++) {
            gameCount = Collections.frequency(ls, ls.get(y));
            if (gameCount > playCount) {
                topGame = ls.get(y);
                playCount = gameCount;
                tie=false;
                ties.clear();
            }else if (gameCount == playCount) {
                tie=true;
                ties.add(ls.get(y));
                System.out.println("tie");
            }

        }
        if(tie){
            System.out.println("tie: "+ties);
            int rand= (int)(Math.random()*ties.size());
            System.out.println("rand: "+rand);
            topGame=ties.get(rand);
            for (Member member : a) {
                if (!member.getActivities().isEmpty()) {
                    if (!member.getUser().isBot() && member.getActivities().get(0).toString().toLowerCase().contains(topGame.toLowerCase())) {
                        players.append(member.getEffectiveName()).append(", ");
                    }
                }
            }

        }else{
            for (Member member : a) {
                if (!member.getActivities().isEmpty()) {
                    if (!member.getUser().isBot() && member.getActivities().get(0).toString().toLowerCase().contains(topGame.toLowerCase())) {
                        players.append(member.getEffectiveName()).append(", ");
                    }
                }
            }

        }
        tip = tipDB.getRandomTip(topGame.toLowerCase().replaceAll("\\s", ""),players.toString());

        if (Objects.equals(tip, "empty")) {
            System.out.println("No tips for current game "+topGame);
            return "No tips for current game"+topGame;
        }
            if(AIc.ttsCall(tip,"output")) {
                if (!self.getVoiceState().inAudioChannel()) {
                    client.getGuildById(guild).getAudioManager().openAudioConnection(vcID);
                } else {
                    if (!self.getVoiceState().getChannel().equals(vcID)) {
                        client.getGuildById(guild).getAudioManager().openAudioConnection(vcID);
                    }
                }
                PlayerManager playerManager = PlayerManager.get();
                System.out.println("tip sent");
                playerManager.play(client.getGuildById(guild), "output.mp3");
                return "The top game is " + topGame + " with " + playCount + " players.";
            }else{
                return "error";
            }
    }
}

