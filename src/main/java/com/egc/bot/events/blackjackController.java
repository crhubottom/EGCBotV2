package com.egc.bot.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.egc.bot.Bot.*;

public class blackjackController {
    private static final String[] RANKS = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private static final int DECKS = 7; // 7 decks = 28 of each rank, same as before
    public static final long BLACKJACK_CHANNEL_ID = 1269464838472597577L;

    public static boolean isBlackjackChannel(long channelID) {
        return channelID == BLACKJACK_CHANNEL_ID;
    }

    // Instance fields (were static) so two games can't overwrite each other's hands
    public ArrayList<String> deck = new ArrayList<>();
    public ArrayList<String> playerHand = new ArrayList<>();
    public ArrayList<String> dealerHand = new ArrayList<>();
    public int gold;
    public Long id;
    public boolean keepDrawing = true;
    public boolean dealer = false;
    private boolean finished = false;
    private long startedAt = 0;
    private static final long ABANDON_AFTER_MS = 5 * 60 * 1000; // a game nobody touches for 5 minutes stops blocking new ones

    // True while someone is mid-hand (dealt, not finished, and not abandoned)
    public synchronized boolean isInProgress() {
        return !playerHand.isEmpty()
                && !finished
                && System.currentTimeMillis() - startedAt < ABANDON_AFTER_MS;
    }

    public blackjackController() {
        reset();
    }

    // Returns null if another game is still in progress
    public synchronized EmbedBuilder start(int gold, Long id) throws SQLException {
        if (isInProgress()) {
            return null;
        }
        reset(); // always start from a fresh deck and empty hands
        this.gold = gold;
        this.id = id;
        startedAt = System.currentTimeMillis();

        playerHand.add(draw());
        dealerHand.add(draw());
        playerHand.add(draw());
        dealerHand.add(draw());

        inv.DeleteItem(id, "Gold", gold);

        String board = "Dealer:\n" + dealerHand.get(0) + " *\n\nPlayer:\n" + handText(playerHand) + "\n\n Stand or Hit?";
        return embed(Color.white, board);
    }

    public synchronized void hit(Long messageID, Long channelID) {
        if (finished || !isBlackjackChannel(channelID)) {
            return; // ignore clicks after the game is over or outside the blackjack channel
        }
        playerHand.add(draw());
        int total = bestTotal(playerHand);

        if (total > 21) {
            finished = true;
            String board = "Dealer:\n" + dealerHand.get(0) + " *\n\nPlayer:\n" + handText(playerHand) + "\n\nBust!";
            edit(channelID, messageID, embed(Color.red, board), true, 0);
        } else if (total == 21) {
            // Nothing to gain by hitting on 21, so stand automatically
            try {
                stand(messageID, channelID);
            } catch (SQLException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            String board = "Dealer:\n" + dealerHand.get(0) + " *\n\nPlayer:\n" + handText(playerHand) + "\n\n Stand or Hit?";
            edit(channelID, messageID, embed(Color.white, board), false, 0);
        }
    }

    // Still declares InterruptedException so existing callers that catch it keep compiling
    public synchronized void stand(Long messageID, Long channelID) throws InterruptedException, SQLException {
        if (finished || !isBlackjackChannel(channelID)) {
            return; // prevents double payouts, and ignores games outside the blackjack channel
        }
        finished = true;
        dealer = true;

        int delay = 0;
        edit(channelID, messageID, embed(Color.white, board()), false, delay);

        // Dealer draws to 17 (stands on all 17s, including soft 17)
        while (bestTotal(dealerHand) < 17) {
            dealerHand.add(draw());
            delay += 3;
            edit(channelID, messageID, embed(Color.white, board()), false, delay);
        }
        keepDrawing = false;

        int player = bestTotal(playerHand);
        int dealerTotal = bestTotal(dealerHand);

        String result;
        Color color;
        int payout;
        if (dealerTotal > 21) {
            result = "Dealer Busts!\nYou Win!";
            color = Color.green;
            payout = gold * 2;
        } else if (player > dealerTotal) {
            result = "You Win!";
            color = Color.green;
            payout = gold * 2;
        } else if (player < dealerTotal) {
            result = "Dealer Wins!";
            color = Color.red;
            payout = 0;
        } else {
            result = "Draw!";
            color = Color.white;
            payout = gold;
        }

        if (payout > 0) {
            inv.AddItem(id, "Gold", payout);
        }

        delay += 5;
        edit(channelID, messageID, embed(color, board() + "\n\n" + result), true, delay);
    }

    // Kept for compatibility: returns "bust", a plain total, or "low/high" for a soft hand
    public String calculateNum(ArrayList<String> hand) {
        int[] value = handValue(hand);
        int total = value[0];
        boolean soft = value[1] > 0;
        if (total > 21) {
            return "bust";
        }
        if (soft) {
            return (total - 10) + "/" + total;
        }
        return String.valueOf(total);
    }

    public synchronized void reset() {
        deck = newDeck();
        playerHand = new ArrayList<>();
        dealerHand = new ArrayList<>();
        keepDrawing = true;
        dealer = false;
        finished = false;
        startedAt = 0;
    }

    // ---------- helpers ----------

    private static ArrayList<String> newDeck() {
        ArrayList<String> d = new ArrayList<>();
        for (int i = 0; i < 4 * DECKS; i++) {
            Collections.addAll(d, RANKS);
        }
        Collections.shuffle(d);
        return d;
    }

    private String draw() {
        if (deck.isEmpty()) {
            deck = newDeck();
        }
        return deck.remove(0);
    }

    // Returns {best total, number of aces still counted as 11}
    private static int[] handValue(List<String> hand) {
        int total = 0;
        int aces = 0;
        for (String card : hand) {
            switch (card) {
                case "A":
                    total += 11;
                    aces++;
                    break;
                case "J", "Q", "K", "10":
                    total += 10;
                    break;
                default:
                    total += Integer.parseInt(card);
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return new int[]{total, aces};
    }

    private static int bestTotal(List<String> hand) {
        return handValue(hand)[0];
    }

    private String handText(ArrayList<String> hand) {
        return String.join(" ", hand) + " = " + calculateNum(hand);
    }

    private String board() {
        return "Dealer:\n" + handText(dealerHand) + "\n\nPlayer:\n" + handText(playerHand);
    }

    private EmbedBuilder embed(Color color, String description) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Blackjack    (" + gold + " gold)", null);
        eb.setColor(color);
        eb.setDescription(description);
        return eb;
    }

    // Uses queueAfter instead of Thread.sleep so the bot isn't frozen while the dealer draws
    private void edit(Long channelID, Long messageID, EmbedBuilder eb, boolean removeButtons, int delaySeconds) {
        if (!isBlackjackChannel(channelID)) {
            return;
        }
        GuildMessageChannel channel = client.getChannelById(GuildMessageChannel.class, channelID);
        if (channel == null) {
            System.out.println("Blackjack channel not found: " + channelID);
            return;
        }
        MessageEditAction action = channel.editMessageEmbedsById(messageID, eb.build());
        if (removeButtons) {
            action = action.setComponents();
        }
        if (delaySeconds > 0) {
            action.queueAfter(delaySeconds, TimeUnit.SECONDS);
        } else {
            action.queue();
        }
    }
}