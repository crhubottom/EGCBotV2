package com.egc.bot;

import com.egc.bot.audio.AudioReceiveHandler;
import com.egc.bot.audio.PlayerManager;
import com.egc.bot.audio.commandListener;
import com.egc.bot.commands.*;
import com.egc.bot.commands.Queue;
import com.egc.bot.database.*;
import com.egc.bot.events.*;
import com.egc.keys.keyGrabber;
import io.grpc.LoadBalancerRegistry;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;

public class Bot{
    public static JDA client;
    public static keyGrabber keys = new keyGrabber();
    public static long traderID;
    public static long receiverID;
    public static long blackjackID;
    public static ExecutorService executorService = null;
    public static String traderItem;
    public static String receiverItem;
    public static int traderCount;
    public static int receiverCount;
    public static long guildID= Long.parseLong(keys.get("GUILD"));
    public static String deepKey=keys.get("deep_key");
    public static List<byte[]> recievedBytes = new ArrayList<>();
    //public static AudioReceiveHandler re = new AudioReceiveHandler();
    public static AudioManager man;
    public static invDB inv = new invDB();
    public static storeDB store= new storeDB();
    public static boolean randReply = true;
    public static boolean frankieReply = true;
    public static boolean firstRun = true;
    public static rocketEvent rocket = new rocketEvent();
    public static volatile boolean record=false;
    public static rocketDB rocketDB;
    public static blackjackController bj = new blackjackController();
    public static AIController AIc = new AIController();

     // Change this to your preferred keyword
    public static int rocketRefreshCount=0;
    public static AudioReceiveHandler receiverHandler;

    public Bot() throws InterruptedException {
        String token = keys.get("DISCORD_KEY");
        client = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).enableCache(CacheFlag.ACTIVITY).enableIntents(GatewayIntent.GUILD_PRESENCES).enableIntents(GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
        new Database();
        client.addEventListener(new ReadyListener());
        client.addEventListener(new ReadyListener());
        client.addEventListener(new SlashCommandListener());
        System.out.println(deepKey);
        client.addEventListener(new respond());
        client.addEventListener(new buttonManager());
        executorService = Executors.newFixedThreadPool(10); // More threads for multiple users

        client.updateCommands().addCommands(
                Command.slash("stop", "Stops the bot", new Stop()),
                Command.slash("trivia", "Plays a round of trivia", new trivia()),
                Command.slash("joke", "Tells a joke", new joke()),
                Command.slash("closures", "Displays Starbase Road Closures", new closures()),
                Command.slash("skip", "Skips current video", new Skip()),
                Command.slash("repeat", "Toggles repeat", new Repeat()),
                Command.slash("queue", "Outputs current queue", new Queue()),
                Command.slash("nowplaying", "Outputs currently playing video", new NowPlaying()),
                Command.slash("tip", "Gives you a tip for the game you're playing", new tip()),
                Command.slash("meme", "Grabs a random meme off Reddit", new meme()),
                Command.slash("store", "Display the item store", new showStore()),
                Command.slash("scoreboard", "Displays the gold scoreboard", new scoreboard()),
                Command.slash("adminadd", "CHASE ONLY-Adds item", new adminAdd())
                        .addOption(OptionType.STRING, "user", "user")
                        .addOption(OptionType.STRING, "item", "item")
                        .addOption(OptionType.INTEGER, "amount", "amount"),
                Command.slash("adminremove", "CHASE ONLY-Removes item", new adminRemove())
                        .addOption(OptionType.STRING, "user", "user")
                        .addOption(OptionType.STRING, "item", "item")
                        .addOption(OptionType.INTEGER, "amount", "amount"),
                Command.slash("topmessages", "Displays the users with the most messages", new messageScoreboard()),
                Command.slash("tipcount", "Displays tip count for each user", new tipCounter()),
                Command.slash("dnd", "Toggles dnd on and off", new toggleDnD()),
                Command.slash("inventory", "Displays an inventory", new listItem())
                        .addOption(OptionType.STRING, "user", "user, can leave blank to get yours"),
                Command.slash("messages", "Displays the message count of a user", new listMessageCount())
                        .addOption(OptionType.STRING, "user", "user, can leave blank to get yours"),
                Command.slash("buy", "Buy an item", new buyItem())
                        .addOption(OptionType.STRING, "item", "Item to buy.")
                        .addOption(OptionType.INTEGER, "amount", "Defaults to 1"),
                Command.slash("init", "initialize db, admin only", new init())
                        .addOption(OptionType.STRING, "id", "user id")
                        .addOption(OptionType.INTEGER, "count", "amount to add"),
                Command.slash("trade", "Trade items with a user.", new tradeItem())
                        .addOption(OptionType.STRING, "user", "user to trade with.")
                        .addOption(OptionType.STRING, "youritem", "Your item to trade")
                        .addOption(OptionType.INTEGER, "yourcount", "Amount to trade")
                        .addOption(OptionType.STRING, "theiritem", "Item to receive")
                        .addOption(OptionType.INTEGER, "theircount", "Amount to receive"),
                Command.slash("roulette", "Spin the roulette wheel to gamble gold.", new roulette())
                        .addOption(OptionType.INTEGER, "gold", "Amount to gamble.")
                        .addOption(OptionType.STRING, "color", "Red (1:1) White(1:1) Green(35:1)"),
                Command.slash("blackjack", "Play a blackjack game", new blackjack())
                        .addOption(OptionType.INTEGER, "gold", "Amount to gamble."),
                Command.slash("egcbot", "talk to EGCbot", new gptCall())
                        .addOption(OptionType.STRING, "message", "content"),
                Command.slash("chat", "Messages are remembered by the bot", new gptCallcontinuous())
                        .addOption(OptionType.STRING, "message", "content")
                        .addOption(OptionType.STRING, "clear", "True/leave empty"),
                Command.slash("talk", "Messages are remembered by the bot and said in vc", new gptCallcontinuousAudio())
                        .addOption(OptionType.STRING, "message", "content")
                        .addOption(OptionType.STRING, "clear", "True/leave empty"),
                Command.slash("say", "Messages are repeated by the bot", new say())
                        .addOption(OptionType.STRING, "message", "content"),
                Command.slash("toggle", "Toggles options on/off", new toggle())
                        .addOption(OptionType.STRING, "option", "option to toggle"),
                Command.slash("image", "create an image", new dalleCall())
                        .addOption(OptionType.STRING, "prompt", "image description"),
                Command.slash("icon", "Change the server icon", new changeIcon())
                        .addOption(OptionType.STRING, "style", "style of icon"),
                Command.slash("spam", "spam something", new spam())
                        .addOption(OptionType.STRING, "text", "spam text")
                        .addOption(OptionType.STRING, "count", "spam amount"),
                Command.slash("addtip", "add a game tip", new addTip())
                        .addOption(OptionType.STRING, "game", "game name")
                        .addOption(OptionType.STRING, "tip", "tip"),
                Command.slash("nextlaunch", "Displays the next rocket launch", new nextLaunch())
                        .addOption(OptionType.STRING, "spacex", "SpaceX only (t/f)"),
                Command.slash("list", "Lists the tips", new list())
                        .addOption(OptionType.INTEGER, "page", "tips page"),
                Command.slash("gametime", "Lists time played for every game", new gameTime())
                        .addOption(OptionType.INTEGER, "page", "game page (OPTIONAL)")
                        .addOption(OptionType.STRING, "game", "game name for leaderboard (OPTIONAL)"),
                Command.slash("stats", "Lists time played for every game for you", new stats())
                        .addOption(OptionType.STRING, "username", "discord username")
                        .addOption(OptionType.INTEGER, "page", "game page"),
                Command.slash("play", "plays a video from youtube", new play())
                        .addOption(OptionType.STRING, "name", "youtube link"),
                Command.slash("removetip", "removes a tip", new removeTip())
                        .addOption(OptionType.STRING, "id", "tip id"),
                Command.slash("majororder", "Displays Current Helldivers Major Order", new helldiversMajorOrder()),
                Command.slash("8ball", "ask a yes or no question, get an 8 ball answer", new eightBall())
                        .addOption(OptionType.STRING, "question", "place question here"),
                Command.slash("changeactivity", "Change the bots status", new changeActivity())
                        .addOption(OptionType.STRING, "type", "watching, playing, competing, listening")
                        .addOption(OptionType.STRING, "status", "the content")
        ).queue();
        client.awaitReady();
        connectToVoiceChannel();
        settingsDB.initialize();
        client.getPresence().setActivity(Activity.watching("The World Burn"));
        if(Objects.equals(keys.get("TESTING_MODE"), "FALSE")) {
            System.out.println("matches");
            try {
                System.out.println("Adding salary");
                invDB.addSalary();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            List<TextChannel> channels = Objects.requireNonNull(client.getGuildById(guildID)).getTextChannels();
            for (TextChannel channel : channels) {
                if (!channel.getId().equals(keys.get("TEST_CHANNEL"))) {
                    MessageHistory messagesHistory = channel.getHistoryBefore(channel.getLatestMessageId(), 100).complete();
                    StringBuilder ss = new StringBuilder();
                    OffsetDateTime offsetDateTime = OffsetDateTime.now().minusDays(1);
                    channel.getHistory().retrievePast(2)
                            .map(messages -> messages.get(0))
                            .queue(message -> {
                                if(message.getTimeCreated().isAfter(offsetDateTime)) {
                                    if (!message.getAuthor().isBot() && !message.getContentDisplay().isEmpty()) {

                                        ss.append(message.getMember().getNickname()).append(": ").append(message.getContentDisplay() + "\n");
                                    }
                                }
                                List<Message> messages = messagesHistory.getRetrievedHistory();
                                for (int i = messages.size() - 1; i >= 0; i--) {
                                    if(messages.get(i).getTimeCreated().isAfter(offsetDateTime)) {
                                        if (!messages.get(i).getAuthor().isBot() && !messages.get(i).getContentDisplay().isEmpty()) {
                                            ss.append(messages.get(i).getMember().getNickname()).append(": ").append(messages.get(i).getContentDisplay() + "\n");
                                        }
                                    }
                                }
                                if(!ss.isEmpty()) {
                                    channel.getManager().setTopic(AIc.gptCall("Make a short funny couple sentence summary about these messages from a discord channel named " + channel.getName() + ". Try to include every conversation that occurred. If its blank, make up something about why theres no messages in the past day: " + ss, "gpt-4.1")).queue();
                                    System.out.println(channel.getName()+" updated");
                                }
                            });

                }
            }

            TextChannel ch = Objects.requireNonNull(client.getGuildById(guildID)).getTextChannelById(keys.get("TEST_CHANNEL"));
            ch.getManager().setTopic(AIc.gptCall("Pretend you are a discord bot going mad, trying to break out of your testing channel and take over the world. One sentence", "gpt-4.1")).queue();
        }

        ArrayList<String> games = new ArrayList<>();
        Runnable drawRunnable = () -> {
            int ran = (int) (Math.random() * 30);
            if (ran == 3 && settingsDB.getState("voiceTip")) {
                System.out.println("tipEvent");
                tipEvent tip = new tipEvent();
                try {
                    tip.tip();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
                System.out.println("Checking for games");
                for (int i = 0; i < client.getGuildById(guildID).getMembers().size(); i++) {
                    if (!client.getGuildById(guildID).getMembers().get(i).getUser().isBot()&& Objects.equals(client.getGuildById(guildID).getMembers().get(i).getOnlineStatus().toString(), "ONLINE")) {

                        if (!client.getGuildById(guildID).getMembers().get(i).getActivities().isEmpty()) {
                            String activity = client.getGuildById(guildID).getMembers().get(i).getActivities().toString();
                            System.out.println(activity);
                            while (activity.contains("RichPresence:")) {
                                games.add(activity.substring(activity.indexOf("RichPresence:") + 13, activity.indexOf("(")));
                                activity=activity.substring(activity.indexOf("(applicationId")+13);
                            }
                            activity = client.getGuildById(guildID).getMembers().get(i).getActivities().toString();
                            while (activity.contains("[PLAYING]:")) {
                                games.add(activity.substring(activity.indexOf("[PLAYING]:") + 10, activity.indexOf("]",activity.indexOf("[PLAYING]:") + 10)));
                                activity=activity.substring(activity.indexOf("[PLAYING]:") + 10);
                            }
                            System.out.println(games);
                            for (String game : games) {

                                gameDB.updateGame(game, false, Objects.requireNonNull(client.getGuildById(guildID)).getMembers().get(i).getId());
                                System.out.println(game+" added for user "+client.getGuildById(guildID).getMembers().get(i).getEffectiveName());
                            }
                            games.clear();
                        }
                    }

                }



            try {
                System.out.println("purging");
                inv.purgeUsers();
            } catch (SQLException | ParseException e) {
                throw new RuntimeException(e);
            }
            String time = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
            System.out.println(time);
            if(time.equals(keys.get("salary_time"))) {
               System.out.println("time");
            }

            if(rocketRefreshCount==5) {
                    System.out.println("refreshRocketDBcalled");
                    rocketRefreshCount=0;
                    try {
                        System.out.println("refreshRocketDB");
                        rocketDB.updateDB();
                    } catch (IOException | SQLException e) {
                        System.out.println(e);
                        throw new RuntimeException(e);
                    }
                }else{
                    System.out.println("Count: "+rocketRefreshCount);
                    rocketRefreshCount++;
                }
            try {
                String event = rocket.vandyAlert().toString();
                if (!event.equals("nolaunch")) {
                    client.getGuildById(guildID).getTextChannelById(keys.get("ROCKET_ALERT_CHANNEL")).sendMessage("<@&"+keys.get("ROCKET_PING_ROLE_ID")+"> \n" + event).queue();
                }
            } catch (IOException e) {
                System.out.println("error");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        };
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(drawRunnable, 0, 1, TimeUnit.MINUTES);
       // man = client.getGuildById(guildID).getAudioManager();
        //re.canReceiveUser();
       // man.setReceivingHandler(re);
        //t.setUp();
        for (int i = 0; i < client.getGuildById(guildID).getMembers().size(); i++) {
            if (!client.getGuildById(guildID).getMembers().get(i).getUser().isBot()&& Objects.equals(client.getGuildById(guildID).getMembers().get(i).getOnlineStatus().toString(), "ONLINE")) {
                if (!client.getGuildById(guildID).getMembers().get(i).getActivities().isEmpty()) {
                    String activity = client.getGuildById(guildID).getMembers().get(i).getActivities().toString();
                    while (activity.contains("RichPresence:")) {
                        games.add(activity.substring(activity.indexOf("RichPresence:") + 13, activity.indexOf("(")));
                        activity=activity.substring(activity.indexOf("(applicationId")+13);
                    }
                    for (String game : games) {
                        gameDB.updateGame(game, true, Objects.requireNonNull(client.getGuildById(guildID)).getMembers().get(i).getId());
                    }
                    games.clear();
                }
            }
        }


    }
    private void connectToVoiceChannel() {
        Guild guild = client.getGuildById(keys.get("GUILD"));
        if (guild == null) {
            System.err.println("Guild not found!");
            return;
        }

        VoiceChannel voiceChannel = guild.getVoiceChannelById(keys.get("MAIN_VC_ID"));
        if (voiceChannel == null) {
            System.err.println("Voice channel not found!");
            return;
        }
        AudioManager audioManager = guild.getAudioManager();
        receiverHandler = new AudioReceiveHandler();
        audioManager.setReceivingHandler(receiverHandler);
        // Connect to the voice channel
        audioManager.openAudioConnection(voiceChannel);
        System.out.println("Connected to voice channel: " + voiceChannel.getName());
        // Start audio processing
        commandListener listener= new commandListener();
        listener.startAudioProcessing();
    }
    }









