package com.egc;

import com.egc.bot.Bot;

public class  Main {
    public static void main(String[] args) throws InterruptedException {
        try {
            new Bot();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}