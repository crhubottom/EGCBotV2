package com.egc.bot.jsonTest;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class JsonTester {

    public static void main(String[] args) throws IOException, JSONException {



        JSONObject jsonObject  = new JSONObject(IOUtils.toString(new URL("https://lldev.thespacedevs.com/2.2.0/launch/upcoming/?format=json&limit=3&mode=list&search=SpaceX&location__ids=11"), StandardCharsets.UTF_8));
        String j=jsonObject.toString();
        System.out.println(j);
        boolean found=false;

        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.out.println(dateFormatGmt.format(new Date()));
        JSONArray results = new JSONArray(jsonObject.getJSONArray("results"));
        for (int i = 0; i < results.length(); ++i) {
            if(!found) {
                if (results.getJSONObject(i).getJSONObject("status").getInt("id") == 2) {
                    found=true;
                    String net = results.getJSONObject(i).getString("net");
                    String date = net.substring(0, net.lastIndexOf("T"));
                    String time = net.substring(net.lastIndexOf("T") + 1, net.lastIndexOf("Z"));


                    DateTimeFormatter fmt = DateTimeFormatter.ISO_ZONED_DATE_TIME;
                    Instant event = fmt.parse(net, Instant::from);
                    Instant now = Instant.now();
                    Duration diff = Duration.between(now, event);
                    long minutes = diff.toMinutes();
                    try(InputStream in = new URL(results.getJSONObject(i).getString("image")).openStream()){

                        Files.copy(in, Paths.get("rocket.png"), StandardCopyOption.REPLACE_EXISTING);
                    }
                   // File a=new File("rocket.jpeg");
                    //FileUpload upload = FileUpload.fromData(a, "image.jpeg");
                    System.out.println(minutes);
                    System.out.println(results.getJSONObject(i).getString("name") + "   " + results.getJSONObject(i).getString("location") + ":   " + date + " " + time + "    " + results.getJSONObject(i).getJSONObject("status").getInt("id"));
                }
            }
        }
    }
}