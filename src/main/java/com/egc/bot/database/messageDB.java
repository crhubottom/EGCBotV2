package com.egc.bot.database;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.egc.bot.Bot.client;
import static com.egc.bot.Bot.guildID;

public class messageDB {
    public static void init(Long id, int count){
        try {
            PreparedStatement ps;
            ps = Database.con.prepareStatement("SELECT * FROM messages WHERE id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (!isMyResultSetEmpty(rs)) {
                System.out.println("existing row found");
                if (rs.next()) {
                    String query = "UPDATE messages SET count=? WHERE id=?";
                    PreparedStatement preparedStmt = Database.con.prepareStatement(query);
                    preparedStmt.setInt(1, (rs.getInt("count") + count));
                    preparedStmt.setLong(2, id);
                    preparedStmt.execute(); //removes tip
                    System.out.println("messages added");
                    return;
                }
            }
            System.out.println("no messages, creating row");
            String query = "INSERT INTO messages (id,count) VALUES (?, ?)";
            PreparedStatement preparedStmt = Database.con.prepareStatement(query);
            preparedStmt.setLong(1, id);
            preparedStmt.setInt(2, count);
            preparedStmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean addMessage(long id) {
        try {
            PreparedStatement ps;
            ps = Database.con.prepareStatement("SELECT * FROM messages WHERE id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (!isMyResultSetEmpty(rs)) {
                System.out.println("existing row found");
                if (rs.next()) {
                    String query = "UPDATE messages SET count=? WHERE id=?";
                    PreparedStatement preparedStmt = Database.con.prepareStatement(query);
                    preparedStmt.setInt(1, (rs.getInt("count") + 1));
                    preparedStmt.setLong(2, id);
                    preparedStmt.execute(); //removes tip
                    System.out.println("message added");
                    return true;
                }
            }
            System.out.println("no messages, creating row");
            String query = "INSERT INTO messages (id,count) VALUES (?, ?)";
            PreparedStatement preparedStmt = Database.con.prepareStatement(query);
            preparedStmt.setLong(1, id);
            preparedStmt.setInt(2, 1);
            preparedStmt.execute();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    public static int getCount(Long id) throws SQLException {
        PreparedStatement ps;
        ps = Database.con.prepareStatement("SELECT * FROM messages WHERE id = ?");
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
       if(rs.next()){
                return rs.getInt("count");
            }
        return 0;
    }
    public static EmbedBuilder getScoreboard(){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Total Messages Sent");
        eb.setColor(Color.blue);
        StringBuilder list = new StringBuilder();
        PreparedStatement ps;
        int count=1;
        try {
            ps = Database.con.prepareStatement("SELECT * FROM messages ORDER BY count desc");
            ResultSet rs = ps.executeQuery();
            while (rs.next()&&count<4) {
                System.out.println(client.getGuildById(guildID).getMemberById(rs.getLong("id")).getEffectiveName());
                list.append(count).append(": ").append(client.getGuildById(guildID).getMemberById(rs.getLong("id")).getNickname()).append("      ").append(rs.getInt("count")).append("\n");
                count++;
            }
            eb.setDescription(list.toString());
            return eb;
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
            list.append("Error: Something went wrong with the database.");
            eb.setDescription("error");
            return eb;
        }
    }
    public static boolean isMyResultSetEmpty(ResultSet rs) throws SQLException {
        return (!rs.isBeforeFirst() && rs.getRow() == 0);
    }
}
