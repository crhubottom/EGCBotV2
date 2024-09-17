package com.egc.bot;

import com.egc.bot.database.settingsDB;
import io.github.stefanbratanov.jvm.openai.OpenAIException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.egc.bot.Bot.*;

public class respond extends ListenerAdapter {
    int count = 0;
    private static String answer = null;
    private static Boolean trivia = false;
    private static String triviaChannelID = null;
    Random rand = new Random();
    public static boolean dnd=false;
    public static FileUpload uploadedImage;
    public static StringBuilder story = new StringBuilder();
    public static long id=0;
    public static boolean secondPart=false;
    public void trivia(String answer, String channelID) {
        respond.answer = answer;
        trivia = true;
        triviaChannelID = channelID;
    }
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        //System.out.println(event.getMessage().getContentRaw());
        String message = event.getMessage().getContentRaw();
        //System.out.println(Objects.requireNonNull(event.getMember()).getOnlineStatus());
        if(dnd){
            if(event.getChannel().getId().equals("1268086672420245556")) {
                if(event.getAuthor().getIdLong()!=id){
                    if(id!=0) {
                        story.append("----------------------------------------------------------------------------------------------------------").append("\n");
                    }
                    id=event.getAuthor().getIdLong();
                }
                if(event.getAuthor().isBot()&&!secondPart){
                    story.append("DM").append(":    ").append(event.getMessage().getContentRaw()).append("\n");

                }else {
                    if(event.getMessage().getContentRaw().equals("<@1237574116328865873>")){
                        story.append("The story continues. \n");
                    }else {
                        story.append(event.getMember().getNickname()).append(":    ").append(event.getMessage().getContentRaw()).append("\n");
                    }
                }

            }
        }
        if (!event.getAuthor().getId().equals(keys.get("BOT_ID"))) {
            try {
                inv.addUser(event.getMember().getIdLong());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(event.getChannel().getId().equals("1268086672420245556")){
                if(dnd&&message.contains("<@1237574116328865873>")){
                    TextChannel tc = event.getChannel().asTextChannel();
                    System.out.println(tc.getName());
                    MessageHistory messagesHistory = tc.getHistoryBefore(tc.getLatestMessageId(), 25).complete();
                    List<Message> messages = messagesHistory.getRetrievedHistory();
                    StringBuilder ss = new StringBuilder();
                    tc.getHistory().retrievePast(1).queue(msgs -> {
                        System.out.println(msgs.get(0).getContentDisplay());
                        if (!msgs.get(0).getAuthor().isBot() && !msgs.get(0).getContentDisplay().isEmpty()) {
                            ss.append(msgs.get(0).getMember().getNickname()).append(": ").append(msgs.get(0).getContentDisplay()).append("\n");
                        }
                    });
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        if (!messages.get(i).getContentDisplay().isEmpty()) {
                            ss.append(messages.get(i).getMember().getNickname()).append(": ").append(messages.get(i).getContentDisplay() + "\n");
                        }
                    }
                    System.out.println(ss);
                    String out = AIc.gptCall("Continue the story with one message, do not include \"EGCBOT:\" or any other names in that style. It must be under 2000 characters in length: "+ss,"gpt-4o-mini");
                    try {
                        AIc.dalleCall(AIc.gptCall("Turn this into a short pg dalle prompt: "+out,"gpt-4o-mini"),"image");
                        java.io.File a=new File("image.png");
                         uploadedImage= FileUpload.fromData(a, "image.png");
                    }catch (OpenAIException e){
                        System.out.println(e.getMessage());
                    }
                    if(out.length()>2000){
                        String half2;
                        String half1;
                        half1=out.substring(0,out.length()/2);
                        half2=out.substring(out.length()/2);
                        tc.sendMessage(half1).queue();
                        secondPart=true;
                        tc.sendMessage(half2).addFiles(uploadedImage).queue();
                        secondPart=!secondPart;
                    }else {
                        tc.sendMessage(out).addFiles(uploadedImage).queue();
                    }
                }
            }
            if(message.contains("<@1237574116328865873>")&&!event.getChannel().getId().equals("1268086672420245556")){
                TextChannel tc = event.getChannel().asTextChannel();
                System.out.println(tc.getName());
                MessageHistory messagesHistory = tc.getHistoryBefore(tc.getLatestMessageId(), 40).complete();
                List<Message> messages = messagesHistory.getRetrievedHistory();
                StringBuilder ss = new StringBuilder();
                for (int i = messages.size() - 1; i >= 0; i--) {
                    if (!messages.get(i).getContentDisplay().isEmpty()) {
                        ss.append("\n").append(i+1).append(": ").append(messages.get(i).getMember().getNickname()).append(": ").append(messages.get(i).getContentDisplay());
                    }
                }
                    tc.getHistory().retrievePast(1).queue(msgs -> {
                        System.out.println(msgs.get(0).getContentDisplay());

                            if (!msgs.get(0).getAuthor().isBot() && !msgs.get(0).getContentDisplay().isEmpty()) {
                                //ss.append(msgs.get(0).getMember().getNickname()).append(": ").append(msgs.get(0).getContentDisplay()).append("\n");
                                ss.append("\n(Newest Message) 0: ").append(msgs.get(0).getMember().getNickname()).append(": ").append(msgs.get(0).getContentDisplay()).append("\n");
                                //System.out.println("Jump into this conversation as yourself, EGCBot, with a short response. Act like you were always part of the conversation. Do not mention your name. Dont ask questions: "+ss);
                                System.out.println(ss);
                                tc.sendMessage(AIc.gptCall("Respond to this message as yourself, EGCBot, with a short response: "+message+". Do not mention your name. Dont ask questions. Here is the context to that message: "+ss,"gpt-4o-mini")).queue();
                            }else{
                                System.out.println("ELSE:\n\n"+ss);
                                tc.sendMessage(AIc.gptCall("Respond to this message as yourself, EGCBot, with a short response: "+message+". Do not mention your name. Dont ask questions. Here is the context to that message: "+ss,"gpt-4o-mini")).queue();
                            }

                    });
            }
            if (trivia && event.getChannel().getId().equals(triviaChannelID)) {
                if (Objects.equals(answer, event.getMessage().getContentRaw())) {
                    trivia = false;
                    event.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue();
                    event.getMessage().reply("Correct!").queue();
                } else {
                    event.getMessage().addReaction(Emoji.fromUnicode("U+274C")).queue();
                }
            } else if(!event.getChannel().getId().equals("1268086672420245556")){

                int ran = (int) (Math.random() * 40);
                System.out.println(ran);
                if ((ran == 5 && settingsDB.getState("randReply"))) {
                    TextChannel tc = event.getChannel().asTextChannel();
                    System.out.println(tc.getName());
                    MessageHistory messagesHistory = tc.getHistoryBefore(tc.getLatestMessageId(), 40).complete();
                    List<Message> messages = messagesHistory.getRetrievedHistory();
                    StringBuilder ss = new StringBuilder();
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        if (!messages.get(i).getContentDisplay().isEmpty()) {
                            ss.append("\n").append(i+1).append(": ").append(messages.get(i).getMember().getNickname()).append(": ").append(messages.get(i).getContentDisplay());
                        }
                    }
                    tc.getHistory().retrievePast(1).queue(msgs -> {
                            System.out.println(msgs.get(0).getContentDisplay());
                            if (!msgs.get(0).getContentDisplay().isEmpty()) {

                                    //ss.append(msgs.get(0).getMember().getNickname()).append(": ").append(msgs.get(0).getContentDisplay()).append("\n");
                                    ss.append("\n(Newest Message) 0: ").append(msgs.get(0).getMember().getNickname()).append(": ").append(msgs.get(0).getContentDisplay()).append("\n");
                                    //System.out.println("Jump into this conversation as yourself, EGCBot, with a short response. Act like you were always part of the conversation. Do not mention your name. Dont ask questions: "+ss);
                                    tc.sendMessage(AIc.gptCall("Jump into this conversation as yourself, EGCBot, with a short response. Act like you were always part of the conversation. Do not mention your name. Dont ask questions: \n"+ss,"gpt-4o-mini")).queue();
                                }else{
                                    tc.sendMessage(AIc.gptCall("Jump into this conversation as yourself, EGCBot, with a short response. Act like you were always part of the conversation. Do not mention your name. Dont ask questions: \n"+ss,"gpt-4o-mini")).queue();
                                }

                        });
                    if (event.getMessage().getAuthor().getName().equals("frankie4sd")) {
                        count++;
                        int rand_int1 = rand.nextInt(30);
                        if (rand_int1 == 3 && settingsDB.getState("frankie")) {
                            event.getMessage().reply("https://tenor.com/view/shh-gif-27680056").queue(); // call queue
                        }
                    }
                }
            }
        }
    }
}

