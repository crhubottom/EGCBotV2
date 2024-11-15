package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;
import java.sql.SQLException;
import static com.egc.bot.Bot.inv;

public class roulette implements ICommand {
    public void run(SlashCommandInteraction ctx) throws SQLException, InterruptedException {
        ctx.deferReply().queue();
        if(ctx.getOption("gold")==null||ctx.getOption("color")==null||ctx.getOption("color").getAsString().isEmpty()){
            ctx.getHook().sendMessage("You must fill all fields.").queue();
            return;
        }
        String chosenColor = ctx.getOption("color").getAsString();
        chosenColor=chosenColor.toLowerCase();
        if(!chosenColor.equals("red")&&!chosenColor.equals("green")&&!chosenColor.equals("white")){
            ctx.getHook().sendMessage("You must enter a correct color.").queue();
            return;
        }
        if(ctx.getOption("gold").getAsInt()<1){
            ctx.getHook().sendMessage("You must enter a correct amount of gold.").queue();
            return;
        }
        if(inv.checkItem(ctx.getMember().getIdLong(),"Gold",ctx.getOption("gold").getAsInt())){
            int gold=ctx.getOption("gold").getAsInt();
            int rollNum=(int)(Math.random()*100);
            String color;
            if(rollNum<49){
                color="white";
            }else if(rollNum>51){
                color="red";
            }else{
                color="green";
            }
            String spin1=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square:⬜\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String spin2=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n:red_square:⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square:\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";

            String green1=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square::green_square: " +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String green2=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n:red_square:⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square::green_square::red_square:" +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String green3=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square::green_square::red_square:⬜" +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String green4=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n:red_square:⬜:red_square:⬜:red_square:⬜:red_square::green_square::red_square:⬜:red_square:" +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String green5=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n⬜:red_square:⬜:red_square:⬜:red_square::green_square::red_square:⬜:red_square:⬜" +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String green6=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n:red_square:⬜:red_square:⬜:red_square::green_square::red_square:⬜:red_square:⬜:red_square:" +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String green7=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n⬜:red_square:⬜:red_square::green_square::red_square:⬜:red_square:⬜:red_square:⬜" +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String green8=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n:red_square:⬜:red_square::green_square::red_square:⬜:red_square:⬜:red_square:⬜:red_square:" +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String green9=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n⬜:red_square::green_square::red_square:⬜:red_square:⬜:red_square:⬜:red_square:⬜" +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String green10=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n:red_square::green_square::red_square:⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square:" +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";
            String green11=":black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::arrow_down::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square: \n:green_square:⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square:⬜:red_square:" +
                    "\n:black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square::black_large_square:";






            int spins=(int) (Math.random()*10)+20;
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Roulette    ("+ctx.getOption("gold").getAsInt()+" gold on "+ctx.getOption("color").getAsString()+")", null);
            eb.setColor(Color.white);
            //eb.setColor(new Color(0xF40C0C));
            //eb.setColor(new Color(255, 0, 54));
            eb.setDescription(spin1);
            ctx.getHook().sendMessageEmbeds(eb.build()).queue();
            Thread.sleep(2000);
            while(spins>0){
                spins--;
                if(spins%15==0){
                    spins-=11;
                    Thread.sleep(500);
                    eb.setDescription(green1);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(green2);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(green3);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(green4);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(green5);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(green6);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(green7);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(green8);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(green9);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(green10);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(green11);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(spin1);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                }else {
                    eb.setDescription(spin2);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                    Thread.sleep(500);
                    eb.setDescription(spin1);
                    ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                }
            }
            if(color.equals("white")){
                eb.setDescription(spin2);
                ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                
            }else if(color.equals("green")){
                gold=(gold*34);
                Thread.sleep(500);
                eb.setDescription(green1);
                ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                Thread.sleep(500);
                eb.setDescription(green2);
                ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                Thread.sleep(500);
                eb.setDescription(green3);
                ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                Thread.sleep(500);
                eb.setDescription(green4);
                ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                Thread.sleep(500);
                eb.setDescription(green5);
                ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                Thread.sleep(500);
                eb.setDescription(green6);
                ctx.getHook().editOriginalEmbeds(eb.build()).queue();
            }

            if(color.equals(chosenColor)){
                Thread.sleep(2000);
                eb.setDescription("You won "+gold+" gold!");
                eb.setColor(Color.green);
                ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                inv.AddItem(ctx.getMember().getIdLong(),"Gold",gold);
            }else{
                Thread.sleep(2000);
                eb.setDescription("You lost.");
                eb.setColor(Color.red);
                ctx.getHook().editOriginalEmbeds(eb.build()).queue();
                inv.DeleteItem(ctx.getMember().getIdLong(),"Gold",ctx.getOption("gold").getAsInt());
            }
        }else{
            ctx.getHook().sendMessage("You don't have enough Gold").queue();

        }
    }


}