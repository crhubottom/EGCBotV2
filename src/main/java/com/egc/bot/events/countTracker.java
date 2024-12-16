package com.egc.bot.events;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class countTracker {
    int num=0;
    int highScore=0;
    EmbedBuilder eb=new EmbedBuilder();
    public int messageIn(String message) {
        eb.setTitle("Count Restarted");
        eb.setColor(Color.red);
        if(message.matches("-?\\d+")){
            if(Integer.parseInt(message)==num){
                num++;
                return -1;
            }else{
                if(num>highScore){
                    highScore=num;
                    num=0;
                    return highScore-1;
                }
                num=0;
                return highScore-1;
            }
        }else{
            if(num>highScore){
                highScore=num;
                num=0;
                return highScore-1;
            }
            num=0;
            return highScore-1;
        }

    }

}
