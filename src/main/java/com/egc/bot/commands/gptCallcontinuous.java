package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import static com.egc.bot.Bot.AIc;
import static com.egc.bot.Bot.textModel;

public class gptCallcontinuous implements ICommand {
    StringBuilder ss = new StringBuilder();
    public void run(SlashCommandInteraction ctx) {

        ctx.deferReply().queue();
        if(ctx.getOption("message")==null) {
            ctx.getHook().sendMessage("You must enter something in the message box.").queue();
        }else {
            if (ctx.getOption("clear") != null) {
                ss.delete(0, ss.length());
            }
            ss.append("Ignore this(\"role\": \"user\"): From ").append(ctx.getMember().getEffectiveName()).append(": ").append(ctx.getOption("message").getAsString()).append("\n");
            try {
                String out = AIc.gptCall(ss.toString(),textModel);
                ss.append("Ignore this(\"role\": \"assistant\"):").append(out).append("\n");
                System.out.println(ss);
                ctx.getHook().sendMessage(out).queue();
            } catch (Exception e) {
                ctx.getHook().sendMessage("something broke, plz fix me chase").queue();
            }
        }
    }

}