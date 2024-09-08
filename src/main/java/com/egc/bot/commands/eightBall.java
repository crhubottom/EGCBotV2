package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.util.Random;


public class eightBall implements ICommand {

    public void run(SlashCommandInteraction ctx) {
        ctx.deferReply().queue();
        String message = ctx.getOption("question").getAsString();
        String randResponse = askEightBall();
        System.out.println(message + randResponse);
        ctx.getHook().sendMessage(message+"\n8Ball Says: "+randResponse).queue();

    }


    public static String askEightBall() {
        String[] responses = {"It is certain", "Reply hazy, try again", "Don’t count on it", "It is decidedly so", "Ask again later", "My reply is no", "Without a doubt", "Better not tell you now", "My sources say no", "Yes definitely", "Cannot predict now", "Outlook not so good", "You may rely on it", "Concentrate and ask again", "Very doubtful", "As I see it, yes", "Most likely", "Outlook good", "Yes", "Signs point to yes"};

        // Generating a random index to choose a response
        Random rand = new Random();
        int index = rand.nextInt(responses.length);

        return responses[index];
    }
}