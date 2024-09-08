package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import com.egc.bot.respond;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * Basic Ping command
 */
public class trivia extends respond implements ICommand  {

    public void run(SlashCommandInteraction ctx) throws IOException {
        ArrayList<String> answers = new ArrayList<>();
        ctx.deferReply().queue();
        try {
            JSONObject jsonObject = new JSONObject(IOUtils.toString(new URL("https://opentdb.com/api.php?amount=1&type=multiple"), StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            JSONObject result = jsonArray.getJSONObject(0);
            System.out.println(result.get("question"));
            String out = result.get("question").toString();
            System.out.println(out);
            String answerLetter=null;
            out = StringEscapeUtils.unescapeHtml3(out);
            builder.append(out).append("\n\n");
            System.out.println(builder);
            answers.add(StringEscapeUtils.unescapeHtml3(result.get("correct_answer").toString()));
            System.out.println("Correct answer: " + result.get("correct_answer").toString());
            JSONArray incorrectAnswers = result.getJSONArray("incorrect_answers");
            for (int i = 0; i < incorrectAnswers.length(); i++) {
                answers.add(StringEscapeUtils.unescapeHtml3(incorrectAnswers.getString(i)));
            }
            System.out.println(answers);
            Collections.shuffle(answers);
            System.out.println(answers);
            for (int y = 0; y < answers.size(); y++) {
                if(Objects.equals(answers.get(y), result.get("correct_answer").toString())){
                    if(y==0){
                        answerLetter="A";
                    }
                    if(y==1){
                        answerLetter="B";
                    }
                    if(y==2){
                        answerLetter="C";
                    }
                    if(y==3){
                        answerLetter="D";
                    }
                }
            }
            builder.append("A: ").append(answers.get(0)).append('\n');
            builder.append("B: ").append(answers.get(1)).append('\n');
            builder.append("C: ").append(answers.get(2)).append('\n');
            builder.append("D: ").append(answers.get(3)).append('\n');
            System.out.println(builder);
            ctx.getHook().sendMessage(builder.toString()).queue();
            trivia(answerLetter,ctx.getChannelId());
        }catch(IOException e){
            ctx.getHook().sendMessage("Rate limit exceeded, please wait a few seconds then try again.").queue();
        }
    }

}
