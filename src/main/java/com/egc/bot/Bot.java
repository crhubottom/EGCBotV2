package com.egc.bot;

import com.egc.bot.audio.AudioReceiveHandler;
import com.egc.bot.audio.commandListener;
import com.egc.bot.commands.*;
import com.egc.bot.commands.Queue;
import com.egc.bot.database.*;
import com.egc.bot.events.*;
import com.egc.keys.keyGrabber;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.LoadBalancerRegistry;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import moe.kyokobot.libdave.DaveFactory;
import moe.kyokobot.libdave.NativeDaveFactory;
import moe.kyokobot.libdave.jda.LDJDADaveSessionFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.audio.dave.DaveSessionFactory;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot {
    public static JDA client;
    public static keyGrabber keys = new keyGrabber();
    public static long traderID;
    public static long receiverID;
    public static long blackjackID;
    public static ExecutorService executorService = null;
    public static String traderItem;
    public static String receiverItem;
    public static int traderCount;
    public static boolean autoTTS = false;
    public static int receiverCount;
    public static long guildID = Long.parseLong(keys.get("GUILD"));
    public static String deepKey = keys.get("deep_key");
    public static String textModel = "gpt-5.4";
    public static List<byte[]> recievedBytes = new ArrayList<>();
    public static AudioManager man;
    public static invDB inv = new invDB();
    public static storeDB store = new storeDB();
    public static boolean randReply = true;
    public static boolean frankieReply = true;
    public static boolean firstRun = true;
    public static String ElevenLabsapiKey;
    public static rocketEvent rocket = new rocketEvent();
    public static volatile boolean record = false;
    public static AIController.Voice[] voiceArray = new AIController.Voice[0]; // empty instead of null if the voice request fails
    public static rocketDB rocketDB;
    public static blackjackController bj = new blackjackController();
    public static AIController AIc = new AIController();
    public static HashMap<String, String> voiceMap = new HashMap<>();
    public static int rocketRefreshCount = 0;
    public static AudioReceiveHandler receiverHandler;
    public static ArrayList<String> currentVoice = new ArrayList<>();
    public static Random rand = new Random();

    private static final int MAX_TOPIC_LENGTH = 1024; // Discord's limit for channel topics

    public Bot() throws InterruptedException, IOException {
        // Created before any listener is registered, so nothing can use it while it's still null
        executorService = Executors.newFixedThreadPool(10);

        String token = keys.get("DISCORD_KEY");
        DaveFactory daveFactory = new NativeDaveFactory(); // Using native libdave via jni-impl

        DaveSessionFactory daveSessionFactory = new LDJDADaveSessionFactory(daveFactory);
        client = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).enableCache(CacheFlag.ACTIVITY).enableIntents(GatewayIntent.GUILD_PRESENCES).enableIntents(GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL)
                .setAudioModuleConfig(new AudioModuleConfig().withDaveSessionFactory(daveSessionFactory)).build();
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
        new Database();
        currentVoice.add("Random");
        client.addEventListener(new ReadyListener()); // was registered twice, so ready events ran twice
        client.addEventListener(new SlashCommandListener());
        client.addEventListener(new respond());
        client.addEventListener(new joinVoiceEvent());
        client.addEventListener(new buttonManager());
        Runtime.getRuntime().addShutdownHook(
                new Thread(AudioReceiveHandler::shutdown, "vosk-shutdown"));

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
                        .addOption(OptionType.INTEGER, "gold", "Amount to gamble.", true)
                        .addOption(OptionType.STRING, "color", "red (1:1), white (1:1), green (35:1)", true),
                Command.slash("blackjack", "Play a blackjack game", new blackjack())
                        .addOption(OptionType.INTEGER, "gold", "Amount to gamble.", true),
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
                Command.slash("setvoice", "Set a voice for the bot to use", new setVoice())
                        .addOption(OptionType.STRING, "voice", "The number corresponding to the voice you want to set"),
                Command.slash("voices", "List all available voices", new voices()),
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
                        .addOption(OptionType.STRING, "status", "the content"),
                Command.slash("addsound", "Add a playable sound", new addSound())
                        .addOption(OptionType.ATTACHMENT, "file", "The file to upload", true)
                        .addOption(OptionType.STRING, "name", "name of the audio clip", true),
                Command.slash("sounds", "List the playable sounds", new sounds()),
                Command.slash("playsound", "Play a sound", new playSound())
                        .addOption(OptionType.STRING, "name", "name of the sound to play", true),
                Command.slash("removesound", "Remove a sound", new removeSound())
                        .addOption(OptionType.STRING, "name", "name of the sound to remove", true)
        ).queue();
        client.awaitReady();

        loadVoices();

        connectToVoiceChannel();
        settingsDB.initialize();
        client.getPresence().setActivity(Activity.watching("The World Burn"));

        if (Objects.equals(keys.get("TESTING_MODE"), "FALSE")) {
            System.out.println("matches");
            try {
                System.out.println("Adding salary");
                invDB.addSalary();
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateChannelTopics();
        }

        // Record the starting games before the scheduler starts, so the two never run at the same time
        recordGames(true);

        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(Bot::minuteTick, 1, 1, TimeUnit.MINUTES);
    }

    // Runs every minute. Each part has its own try/catch: an exception escaping a
    // scheduleAtFixedRate task silently cancels it forever.
    private static void minuteTick() {
        try {
            int ran = (int) (Math.random() * 30);
            if (ran == 3 && settingsDB.getState("voiceTip")) {
                System.out.println("tipEvent");
                new tipEvent().tip();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Checking for games");
            recordGames(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.out.println("purging");
            inv.purgeUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String time = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
        System.out.println(time);
        if (time.equals(keys.get("salary_time"))) {
            System.out.println("time");
        }

        try {
            if (rocketRefreshCount == 5) {
                rocketRefreshCount = 0;
                System.out.println("refreshRocketDB");
                rocketDB.updateDB();
            } else {
                System.out.println("Count: " + rocketRefreshCount);
                rocketRefreshCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String event = rocket.vandyAlert().toString();
            if (!event.equals("nolaunch")) {
                Guild guild = client.getGuildById(guildID);
                TextChannel alertChannel = guild != null ? guild.getTextChannelById(keys.get("ROCKET_ALERT_CHANNEL")) : null;
                if (alertChannel != null) {
                    alertChannel.sendMessage("<@&" + keys.get("ROCKET_PING_ROLE_ID") + "> \n" + event).queue();
                } else {
                    System.out.println("Rocket alert channel not found");
                }
            }
        } catch (Exception e) {
            System.out.println("Rocket alert error: " + e.getMessage());
        }
    }

    // Uses JDA's activity API instead of parsing toString(), which could crash or record a game twice
    private static void recordGames(boolean initial) {
        Guild guild = client.getGuildById(guildID);
        if (guild == null) {
            return;
        }
        for (Member member : guild.getMembers()) {
            if (member.getUser().isBot() || member.getOnlineStatus() != OnlineStatus.ONLINE) {
                continue;
            }
            Set<String> games = new LinkedHashSet<>(); // local, and no duplicates
            for (Activity activity : member.getActivities()) {
                if (activity.getType() == Activity.ActivityType.PLAYING) {
                    games.add(activity.getName().trim());
                }
            }
            for (String game : games) {
                gameDB.updateGame(game, initial, member.getId());
                if (!initial) {
                    System.out.println(game + " added for user " + member.getEffectiveName());
                }
            }
        }
    }

    private static void loadVoices() {
        ElevenLabsapiKey = System.getenv("ELEVENLABS_API_KEY");
        if (ElevenLabsapiKey == null || ElevenLabsapiKey.isBlank()) {
            System.out.println("Missing ELEVENLABS_API_KEY environment variable.");
            return; // a null header value would crash startup
        }

        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(
                    com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false
            );

            HttpRequest voicesRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.elevenlabs.io/v1/voices"))
                    .header("xi-api-key", ElevenLabsapiKey)
                    .GET()
                    .build();

            HttpResponse<String> voicesResponse = httpClient.send(voicesRequest, HttpResponse.BodyHandlers.ofString());

            if (voicesResponse.statusCode() == 200) {
                AIController.VoicesResponse data = mapper.readValue(voicesResponse.body(), AIController.VoicesResponse.class);
                for (AIController.Voice v : data.voices) {
                    voiceMap.put(v.name, v.voice_id);
                }
                voiceArray = data.voices.toArray(new AIController.Voice[0]);
            } else {
                System.out.println("Voice request failed: " + voicesResponse.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // A network hiccup here shouldn't stop the whole bot from starting
            System.out.println("Voice request failed: " + e.getMessage());
        }
    }

    private static void updateChannelTopics() {
        Guild guild = client.getGuildById(guildID);
        if (guild == null) {
            return;
        }
        String testChannelId = keys.get("TEST_CHANNEL");
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(1);

        for (TextChannel channel : guild.getTextChannels()) {
            if (channel.getId().equals(testChannelId)) continue;

            channel.getHistory().retrievePast(100).queue(
                    // GPT runs on the executor, not JDA's callback thread, so it doesn't stall the bot
                    messages -> executorService.submit(() -> summarizeChannel(channel, messages, cutoff)),
                    error -> System.err.println("Couldn't read " + channel.getName() + ": " + error.getMessage())
            );
        }

        TextChannel testChannel = testChannelId != null ? guild.getTextChannelById(testChannelId) : null;
        if (testChannel != null) {
            executorService.submit(() -> {
                String topic = AIc.gptCall("Pretend you are a discord bot going mad, trying to break out of your testing channel and take over the world. One sentence", textModel);
                setTopic(testChannel, topic);
            });
        }
    }

    private static void summarizeChannel(TextChannel channel, List<Message> messages, OffsetDateTime cutoff) {
        try {
            List<Message> ordered = new ArrayList<>(messages);
            Collections.reverse(ordered); // oldest -> newest

            StringBuilder sb = new StringBuilder();
            for (Message m : ordered) {
                if (m.getTimeCreated().isBefore(cutoff)) continue;
                String content = m.getContentDisplay().trim();
                if (content.isEmpty()) continue;
                String display = m.getMember() != null ? m.getMember().getEffectiveName() : m.getAuthor().getName();
                sb.append(display).append(": ").append(content).append('\n');
            }

            String prompt = "Make a short funny couple sentence summary about these messages from a " +
                    "discord channel named " + channel.getName() +
                    ". Try to include every conversation that occurred. If it's blank, " +
                    "make up something about why there are no messages in the past day:\n" +
                    sb;

            setTopic(channel, AIc.gptCall(prompt, textModel));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Topics over 1024 characters make setTopic throw, so trim them first
    private static void setTopic(TextChannel channel, String topic) {
        if (topic == null || topic.isBlank()) {
            return;
        }
        if (topic.length() > MAX_TOPIC_LENGTH) {
            topic = topic.substring(0, MAX_TOPIC_LENGTH - 3) + "...";
        }
        channel.getManager().setTopic(topic).queue(
                success -> System.out.println(channel.getName() + " updated"),
                error -> System.err.println("Failed to update " + channel.getName() + ": " + error.getMessage())
        );
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
        audioManager.openAudioConnection(voiceChannel);
        System.out.println("Connected to voice channel: " + voiceChannel.getName());
        commandListener listener = new commandListener();
        listener.startAudioProcessing();
    }
}