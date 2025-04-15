package com.egc.bot.commands;

import com.egc.bot.audio.PlayerManager;
import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import static com.egc.bot.Bot.AIc;

public class gptCallcontinuousAudio implements ICommand {
    StringBuilder ss = new StringBuilder();
    public void run(SlashCommandInteraction ctx) {

        ctx.deferReply().queue();
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

        if (ctx.getOption("message") == null) {
            ctx.getHook().sendMessage("You must enter something in the message box.").queue();
        } else {
            if (ctx.getOption("clear") != null) {
                ss.delete(0, ss.length());
            }
            ss.append("Ignore this(\"role\": \"user\"): From ").append(ctx.getMember().getEffectiveName()).append(": ").append(ctx.getOption("message").getAsString()).append("\n");
            try {
                String out= AIc.gptCall(ss.toString(),"gpt-4.1");
                ss.append("Ignore this(\"role\": \"assistant\"):").append(out).append("\n");
                System.out.println(ss);
                if(AIc.ttsCall(out,"output")) {
                    PlayerManager playerManager = PlayerManager.get();
                    playerManager.play(ctx.getGuild(), "output.mp3");
                    ctx.getHook().sendMessage("Success.").queue();
                }else{
                    ctx.getHook().sendMessage("Error.").queue();
                }
            }catch (Exception e){
                System.out.println(e);
            }
        }
    }

}



