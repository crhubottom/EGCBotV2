package com.egc.bot.events;

import com.egc.bot.database.Database;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class rocketEvent {
    public static File upload;
    public static boolean ratelimit=false;
    public static String tminus;
    public static String rcktFormat;
    public StringBuilder vandyAlert() throws IOException, SQLException {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.out.println(dateFormatGmt.format(new Date()));
        StringBuilder out = new StringBuilder();
        PreparedStatement ps = Database.con.prepareStatement("SELECT * FROM launches WHERE location like 'Vandenberg SFB, CA, USA' ORDER BY net LIMIT 1 ");
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            String net = rs.getString("net");
            // String date = net.substring(0, net.lastIndexOf("T"));
            //  String time = net.substring(net.lastIndexOf("T") + 1, net.lastIndexOf("Z"));
            DateTimeFormatter fmt = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            Instant event = fmt.parse(net, Instant::from);
            Instant now = Instant.now();
            Duration diff = Duration.between(now, event);
            long seconds = diff.toSeconds();
            long millis = diff.toMillis();
            System.out.println(seconds);
            //long sec = seconds % 60;
            long minutes = diff.toMinutes();
            switch((int) Math.floor(minutes)) {
                case (5):
                    out.append(rs.getString("mission")).append(" ").append(TimeFormat.RELATIVE.atTimestamp(Instant.now().plusMillis(millis).plusSeconds(5).toEpochMilli()));
                    break;
                case (30):

                    out.append(rs.getString("mission")).append(" ").append(TimeFormat.RELATIVE.atTimestamp(Instant.now().plusMillis(millis).plusSeconds(5).toEpochMilli()));
                    break;
                default:
                    out.delete(0, out.length());
                    out.append("nolaunch");
                    break;
            }
        }
        if(out.isEmpty()){
            out.append("nolaunch");
        }
        return out;
    }
    public static StringBuilder nextLaunch(boolean all, boolean voice_format) throws IOException, SQLException {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.out.println(dateFormatGmt.format(new Date()));
        StringBuilder out = new StringBuilder();
        PreparedStatement ps;
        boolean found=false;
        if(all) {
            ps = Database.con.prepareStatement("SELECT * FROM launches ORDER BY net LIMIT 5 ");
        }else{
            ps = Database.con.prepareStatement("SELECT * FROM launches WHERE LSP LIKE 'SpaceX' ORDER BY net LIMIT 5 ");
        }
        ResultSet rs = ps.executeQuery();
        while(!found) {
            rs.next();
            String net = rs.getString("net");
            DateTimeFormatter fmt = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            Instant event = fmt.parse(net, Instant::from);
            Instant now = Instant.now();
            Duration diff = Duration.between(now, event);
            long seconds = diff.toSeconds();
            long millis = diff.toMillis();
            if (seconds > 0) {
                found = true;
                out = new StringBuilder();
                System.out.println(seconds);
                long minutes = seconds % 3600 / 60;
                long hours = seconds % 86400 / 3600;
                long days = seconds / 86400;

                System.out.println(TimeFormat.RELATIVE.atTimestamp(Instant.now().plusMillis(millis).plusSeconds(5).toEpochMilli()));

                if(!voice_format) {
                    System.out.println(days + ":" + hours + ":" + minutes);
                    tminus = ("T-") + (days) + (":") + (hours) + (":") + (minutes);
                    out.append("\nMission: ").append(rs.getString("mission"));
                    out.append("\nAgency: ").append(rs.getString("LSP"));
                    out.append("\nLocation: ").append(rs.getString("location"));
                    out.append("\nTime: ").append(TimeFormat.DATE_TIME_LONG.atTimestamp(Instant.now().plusMillis(millis).plusSeconds(5).toEpochMilli()));
                    String format = rs.getString("image").substring(rs.getString("image").lastIndexOf("."));
                    try (InputStream in = new URL(rs.getString("image")).openStream()) {
                        Files.copy(in, Paths.get("rocket" + format), StandardCopyOption.REPLACE_EXISTING);
                    }
                    File a = new File("rocket" + format);
                    upload = a;
                    rcktFormat = format;
                }else if(all){
                    out.append("The next launch is ").append(rs.getString("LSP")).append(" launching ").append(rs.getString("mission")).append(" from ").append(rs.getString("location")).append(" in ").append(("T minus")).append(days).append(" days, ").append(hours).append(" hours, and ").append(minutes).append(" minutes.");
                }else{
                    out.append("The next SpaceX launch is ").append(rs.getString("mission")).append(" from ").append(rs.getString("location")).append(" in ").append(("T minus")).append(days).append(" days, ").append(hours).append(" hours, and ").append(minutes).append(" minutes.");

                }

            }
        }
                return out;
                }

public static StringBuilder nextLaunchWithLSP(String LSP) throws IOException, SQLException {
    SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    System.out.println(dateFormatGmt.format(new Date()));
    StringBuilder out = new StringBuilder();
    PreparedStatement ps;
    boolean found=false;
    ps = Database.con.prepareStatement("SELECT * FROM launches WHERE LSP LIKE ? ORDER BY net LIMIT 1 ");
    ps.setString(1, LSP);
    ResultSet rs = ps.executeQuery();
    while(rs.next()) {
        String net = rs.getString("net");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        Instant event = fmt.parse(net, Instant::from);
        Instant now = Instant.now();
        Duration diff = Duration.between(now, event);
        long seconds = diff.toSeconds();
        long millis = diff.toMillis();
        if (seconds > 0) {
            found = true;
            out = new StringBuilder();
            System.out.println(seconds);
            //long sec = seconds % 60;
            long minutes = seconds % 3600 / 60;
            long hours = seconds % 86400 / 3600;
            long days = seconds / 86400;
            System.out.println(TimeFormat.RELATIVE.atTimestamp(Instant.now().plusMillis(millis).plusSeconds(5).toEpochMilli()));
                out.append("The next "+LSP+" launch is ").append(rs.getString("mission")).append(" from ").append(rs.getString("location")).append(" in ").append(("T minus")).append(days).append(" days, ").append(hours).append(" hours, and ").append(minutes).append(" minutes.");
        }

    }
if(!found){
    out.append("There are no upcoming launches from ").append(LSP);
}
    return out;
    }




}






