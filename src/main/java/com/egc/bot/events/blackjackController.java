package com.egc.bot.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static com.egc.bot.Bot.*;

public class blackjackController {
    public static ArrayList<String> deck = new ArrayList<>(
            Arrays.asList("A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A",
                    "2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2",
                    "3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3",
                    "4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4",
                    "5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5",
                    "6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6",
                    "7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7",
                    "8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8",
                    "9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9",
                    "10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10",
                    "J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J",
                    "Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q",
                    "K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K")
    );
    public static ArrayList<String> playerHand =new ArrayList<>();
    public static ArrayList<String> dealerHand =new ArrayList<>();
    public static int gold;
    public Long id;
    public boolean keepDrawing=true;
    public boolean dealer=false;
    public EmbedBuilder start(int gold,Long id) throws SQLException {

        Collections.shuffle(deck);
        System.out.println(deck.size());
        playerHand.add(deck.get(0));
        deck.remove(0);
        dealerHand.add(deck.get(0));
        deck.remove(0);
        if(playerHand.contains("A")){
            while(Objects.equals(deck.get(0), "A")){
                Collections.shuffle(deck);
            }
        }
        playerHand.add(deck.get(0));
        deck.remove(0);
        if(dealerHand.contains("A")){
            while(Objects.equals(deck.get(0), "A")){
                Collections.shuffle(deck);
            }
        }
        dealerHand.add(deck.get(0));
        deck.remove(0);
    blackjackController.gold = gold;
         this.id=id;
        StringBuilder ss=new StringBuilder();
        ss.append("Dealer:\n").append(dealerHand.get(0)).append(" *\n\n").append("Player:\n");
        for (String s : playerHand) {
            ss.append(s).append(" ");
        }
        ss.append("= ").append(calculateNum(playerHand));
        ss.append("\n\n Stand or Hit?");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Blackjack    ("+gold+" gold)", null);
        eb.setColor(Color.white);
        eb.setDescription(ss);
        inv.DeleteItem(id,"Gold",gold);
        return eb;
    }
    public void hit(Long messageID,Long channelID){
        if(playerHand.contains("A")){
            while(Objects.equals(deck.get(0), "A")){
                Collections.shuffle(deck);
            }
        }
        playerHand.add(deck.get(0));
        deck.remove(0);
        String pl1=calculateNum(playerHand);
        if(!Objects.equals(pl1, "bust")) {
            StringBuilder ss = new StringBuilder();
            int playerNum = 0;
            int dealerNum = 0;
            ss.append("Dealer:\n").append(dealerHand.get(0)).append(" *\n\n").append("Player:\n");
            for (String s : playerHand) {
                ss.append(s).append(" ");
            }
            ss.append("= ").append(pl1);
            ss.append("\n\n Stand or Hit?");
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Blackjack    (" + gold + " gold)", null);
            eb.setColor(Color.white);
            eb.setDescription(ss);
            client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).queue();
        }else{
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Blackjack    (" + gold + " gold)", null);
            eb.setColor(Color.red);
            eb.setDescription("Bust!");
            client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();

        }

    }
    public void stand(Long messageID,Long channelID) throws InterruptedException, SQLException {
        StringBuilder ss = new StringBuilder();

        ss.append("Dealer:\n");
        for (String s : dealerHand) {
            ss.append(s).append(" ");
        }
        ss.append("\n\nPlayer:\n");
        for (String s : playerHand) {
            ss.append(s).append(" ");
        }
        ss.append("= ").append(calculateNum(playerHand));
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Blackjack    (" + gold + " gold)", null);
        eb.setColor(Color.white);
        eb.setDescription(ss);
        client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).queue();
        System.out.println("Player End Hand:");
        String pl=calculateNum(playerHand);
        dealer=true;
        System.out.println("Dealer Original Hand:");
        calculateNum(dealerHand);
        while(keepDrawing){
            System.out.println("New Card: "+deck.get(0));
            Thread.sleep(3000);
            if(dealerHand.contains("A")){
                while(Objects.equals(deck.get(0), "A")){
                    Collections.shuffle(deck);
                }
            }
            dealerHand.add(deck.get(0));
            deck.remove(0);
             ss = new StringBuilder();
            ss.append("Dealer:\n");
            for (String s : dealerHand) {
                ss.append(s).append(" ");
            }
            System.out.println("Dealer Draws:");
            ss.append("= ").append(calculateNum(dealerHand));
            ss.append("\n\nPlayer:\n");
            for (String s : playerHand) {
                ss.append(s).append(" ");
            }
            ss.append("= ").append(pl);
            eb = new EmbedBuilder();
            eb.setTitle("Blackjack    (" + gold + " gold)", null);
            eb.setColor(Color.white);
            eb.setDescription(ss);
            client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).queue();
        }
        Thread.sleep(5000);
        if(Objects.equals(calculateNum(dealerHand), "bust")){
             eb = new EmbedBuilder();
            eb.setTitle("Blackjack    (" + gold + " gold)", null);
            eb.setColor(Color.green);
            eb.setDescription("Dealer Busts!\nYou Win!");
            inv.AddItem(id,"Gold",gold*2);
            client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
        }else{
            if(calculateNum(dealerHand).contains("/")&&calculateNum(playerHand).contains("/")) {
                int player1;
                int player2;
                int dealer1;
                int dealer2;
                dealer1 = Integer.parseInt(calculateNum(dealerHand).substring(0, calculateNum(dealerHand).indexOf("/")));
                dealer2 = Integer.parseInt(calculateNum(dealerHand).substring(calculateNum(dealerHand).indexOf("/")+1));
                player1 = Integer.parseInt(calculateNum(playerHand).substring(0, calculateNum(playerHand).indexOf("/")));
                player2 = Integer.parseInt(calculateNum(playerHand).substring(calculateNum(playerHand).indexOf("/")+1));
                int dealer=0;
                int player=0;
                if (dealer1 > dealer2 && dealer1 <= 21) {
                    dealer = dealer1;
                }
                if (dealer2 > dealer1 && dealer2 <= 21) {
                    dealer = dealer2;
                }
                if (player1 > player2 && player1 <= 21) {
                    player = player1;
                }
                if (player2 > player1 && player2 <= 21) {
                    player = player2;
                }
                if (dealer > player) {
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.red);
                    eb.setDescription("Dealer Wins!");
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }else if(player>dealer){
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.green);
                    eb.setDescription("You Win!");
                    inv.AddItem(id,"Gold",gold*2);
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }else{
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.white);
                    eb.setDescription("Draw!");
                    inv.AddItem(id,"Gold",gold);
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }

            }else if(calculateNum(dealerHand).contains("/")) {
                int dealer1;
                int dealer2;
                dealer1 = Integer.parseInt(calculateNum(dealerHand).substring(0, calculateNum(dealerHand).indexOf("/")));
                dealer2 = Integer.parseInt(calculateNum(dealerHand).substring(calculateNum(dealerHand).indexOf("/")+1));
                int dealer=0;
                if (dealer1 > dealer2 && dealer1 <= 21) {
                    dealer = dealer1;
                }
                if (dealer2 > dealer1 && dealer2 <= 21) {
                    dealer = dealer2;
                }
                if(dealer > Integer.parseInt(calculateNum(playerHand))){
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.red);
                    eb.setDescription("Dealer Wins!");
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }else if(dealer < Integer.parseInt(calculateNum(playerHand))){
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.green);
                    eb.setDescription("You Win!");
                    inv.AddItem(id,"Gold",gold*2);
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }else{
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.white);
                    eb.setDescription("Draw!");
                    inv.AddItem(id,"Gold",gold);
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }
            }else if(calculateNum(playerHand).contains("/")){
                int player1;
                int player2;
                player1 = Integer.parseInt(calculateNum(playerHand).substring(0, calculateNum(playerHand).indexOf("/")));
                player2 = Integer.parseInt(calculateNum(playerHand).substring(calculateNum(playerHand).indexOf("/")+1));
                int player=0;
                if (player1 > player2 && player1 <= 21) {
                    player = player1;
                }
                if (player2 > player1 && player2 <= 21) {
                    player = player2;
                }
                if(player < Integer.parseInt(calculateNum(dealerHand))){
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.red);
                    eb.setDescription("Dealer Wins!");
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }else if(player > Integer.parseInt(calculateNum(dealerHand))){
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.green);
                    eb.setDescription("You Win!");
                    inv.AddItem(id,"Gold",gold*2);
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }else{
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.white);
                    eb.setDescription("Draw!");
                    inv.AddItem(id,"Gold",gold);
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }
            }else{
                if(Integer.parseInt(calculateNum(dealerHand))==Integer.parseInt(calculateNum(playerHand))){
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.white);
                    eb.setDescription("Draw!");
                    inv.AddItem(id,"Gold",gold);
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }else if(Integer.parseInt(calculateNum(dealerHand))>Integer.parseInt(calculateNum(playerHand))){
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.red);
                    eb.setDescription("Dealer Wins!");

                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }else{
                    eb = new EmbedBuilder();
                    eb.setTitle("Blackjack    (" + gold + " gold)", null);
                    eb.setColor(Color.green);
                    eb.setDescription("You Win!");
                    inv.AddItem(id,"Gold",gold*2);
                    client.getGuildById(guildID).getTextChannelById(channelID).editMessageEmbedsById(messageID, eb.build()).setComponents().queue();
                }
            }
        }

    }
    public String calculateNum(ArrayList<String> s){
        int ace11=0;
        int ace1=0;
        boolean ace=false;
        for (String card : s) {
            System.out.println(ace11+" "+ace1+" "+card);
            switch(card){
                case "A":
                    ace1++;
                    ace11+=11;
                    ace=true;
                    break;
                case "J","K","Q","10":
                    ace1+=10;
                    ace11+=10;
                    break;
                case "2":
                    ace1+=2;
                    ace11+=2;
                    break;
                case "3":
                    ace1+=3;
                    ace11+=3;
                    break;
                case "4":
                    ace1+=4;
                    ace11+=4;
                    break;
                case "5":
                    ace1+=5;
                    ace11+=5;
                    break;
                case "6":
                    ace1+=6;
                    ace11+=6;
                    break;
                case "7":
                    ace1+=7;
                    ace11+=7;
                    break;
                case "8":
                    ace1+=8;
                    ace11+=8;
                    break;
                case "9":
                    ace1+=9;
                    ace11+=9;
                    break;
            }

        }
        System.out.println("Total: "+ace1+" "+ace11);
        if(((ace1>=17&&ace11>=17)||ace1==21||ace11==21)&&dealer){
            System.out.println("stop drawing");
            keepDrawing=false;
        }
        if(ace){
            if(ace1>21&&ace11>21){
                return "bust";
            }else {
                return ace1 + "/" + ace11;
            }
        }else if(ace1>21){
            return "bust";
        }else{
            return String.valueOf(ace1);

        }
    }
    public void reset(){
        deck = new ArrayList<>(
                Arrays.asList("A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A","A",
                        "2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2",
                        "3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3","3",
                        "4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4","4",
                        "5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5","5",
                        "6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6","6",
                        "7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7","7",
                        "8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8",
                        "9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9","9",
                        "10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10","10",
                        "J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J","J",
                        "Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q","Q",
                        "K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K","K")
        );
        playerHand =new ArrayList<>();
        dealerHand =new ArrayList<>();
        keepDrawing=true;
        dealer=false;
    }
}
