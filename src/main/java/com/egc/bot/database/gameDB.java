package com.egc.bot.database;



import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

import static com.egc.bot.Bot.client;
import static com.egc.bot.Bot.guildID;


public class gameDB {
    private final String gameName;

    public gameDB(String gameName) {

        this.gameName = gameName;
    }


    public static boolean updateGame(String gameName, boolean init, String user) {
        PreparedStatement ps;

        String username = client.getGuildById(guildID).getMemberById(user).getUser().getName().replaceAll("\\s+", "").replaceAll("[,.]", "");

        try {
            ps = Database.con.prepareStatement("SELECT game FROM games WHERE game = ?");
            ps.setString(1, gameName);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                String query = "INSERT INTO games (game,timePlayed) VALUES (?, ?)";
                PreparedStatement preparedStmt = Database.con.prepareStatement(query);

                preparedStmt.setString(1, gameName);
                preparedStmt.setInt(2, 0);
                preparedStmt.execute();
                return true;
            } else {
                if (!init && Objects.equals(client.getGuildById(guildID).getMemberById(user).getOnlineStatus().toString(), "ONLINE")) {
                    String query = "UPDATE games SET timePlayed = timePlayed + 1 WHERE game = ?";
                    PreparedStatement preparedStmt = Database.con.prepareStatement(query);
                    preparedStmt.setString(1, gameName);
                    preparedStmt.execute();
                    String query1 = "SHOW TABLES LIKE ?";
                    preparedStmt = Database.con.prepareStatement(query1);
                    preparedStmt.setString(1, client.getGuildById(guildID).getMemberById(user).getUser().getName().replaceAll("\\s+", "").replaceAll("[,.]", ""));

                    preparedStmt.execute();
                    ResultSet rs1 = preparedStmt.executeQuery();
                    if (!rs1.next()) {
                        System.out.println("table for " + user + " not found");
                        String table = "CREATE TABLE " + client.getGuildById(guildID).getMemberById(user).getUser().getName().replaceAll("\\s+", "").replaceAll("[,.]", "") + " (game varchar(150) NOT NULL,timePlayed int default 0,discordid BIGINT default " + user + ", KEY (game))";
                        preparedStmt = Database.con.prepareStatement(table);
                        preparedStmt.execute();
                    }
                    ps = Database.con.prepareStatement("SELECT game FROM " + username + " WHERE game = ?");
                    ps.setString(1, gameName);
                    rs = ps.executeQuery();
                    if (!rs.next()) {
                        String query2 = "INSERT INTO " + username + " (game,timePlayed) VALUES (?, ?)";
                        preparedStmt = Database.con.prepareStatement(query2);
                        preparedStmt.setString(1, gameName);
                        preparedStmt.setInt(2, 1);
                        preparedStmt.execute();
                        return true;
                    } else {

                        query = "UPDATE " + username + " SET timePlayed = timePlayed + 1 WHERE game = ?";
                        preparedStmt = Database.con.prepareStatement(query);
                        preparedStmt.setString(1, gameName);
                        preparedStmt.execute();
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
        }
        return false;
    }

    public static ArrayList<StringBuilder> list() {
        ArrayList<StringBuilder> pages = new ArrayList<>();
        StringBuilder tips = new StringBuilder();
        PreparedStatement ps;
        pages.add(tips);
        int page = 0;
        int lines = 0;
        try {
            ps = Database.con.prepareStatement("SELECT * FROM games ORDER BY timePlayed DESC");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (lines < 10) {
                    lines++;
                    pages.get(page).append(lines + (page * 10) + ": " + rs.getString("game") + ": " + rs.getInt("timePlayed") / 24 / 60 + ":" + rs.getInt("timePlayed") / 60 % 24 + ':' + rs.getInt("timePlayed") % 60 + "\n");

                } else {
                    page++;
                    lines = 0;
                    pages.add(new StringBuilder());
                }
            }
            if (tips.isEmpty()) {
                pages.get(0).append("There are no games to display.");
                return pages;
            } else {
                return pages;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
            pages.get(0).append("Error: Something went wrong with the database.");
            return pages;
        }

    }
    public static StringBuilder topGame() {

        StringBuilder game = new StringBuilder();
        PreparedStatement ps;


        try {
            ps = Database.con.prepareStatement("SELECT * FROM games ORDER BY timePlayed DESC LIMIT 1");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
               game.append(rs.getString("game")).append(" with ").append(rs.getInt("timePlayed") / 24 / 60).append(" days, ").append(rs.getInt("timePlayed") / 60 % 24).append("hours, and ").append(rs.getInt("timePlayed") % 60).append(" minutes.");
            }
            return game;

        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
            game.append("Error: Something went wrong with the database.");
            return game;
        }

    }
    public static StringBuilder listgame(String gameName) {

        StringBuilder ss = new StringBuilder();
        PreparedStatement ps;
        try {
            ps = Database.con.prepareStatement("SHOW TABLES WHERE tables_in_egcdb not like '%games' and tables_in_egcdb not like 'tips%' and tables_in_egcdb not like 'users%';");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ps = Database.con.prepareStatement("SELECT * FROM " + rs.getString(1));
                ResultSet res = ps.executeQuery();
                while (res.next()) {
                    if (res.getString("game").equals(gameName)) {
                        ss.append(rs.getString(1)).append(": ").append(res.getInt("timePlayed")).append(" minutes.\n");
                    }
                }
            }
        } catch (SQLException e) {
System.err.println("ERROR: Something went wrong with the database.");
        }
        if(ss.isEmpty()){
            return ss.append("Game has never been played.");
        }
        return ss;
    }

    public static ArrayList<StringBuilder> listUser(String username) {
        ArrayList<StringBuilder> pages = new ArrayList<>();
        StringBuilder tips = new StringBuilder();
        PreparedStatement ps;
        pages.add(tips);
        int page = 0;
        int lines =0;
        try {
            ps = Database.con.prepareStatement("SELECT * FROM "+username+" ORDER BY timePlayed DESC");
            ResultSet rs = ps.executeQuery();
            //pages.get(0).append("# Stats for "+username+"\n");
            while (rs.next()) {
                if (lines<10) {
                    lines++;
                    pages.get(page).append(lines+": "+rs.getString("game") + ": " + rs.getInt("timePlayed")/24/60 + ":" + rs.getInt("timePlayed")/60%24 + ':' + rs.getInt("timePlayed")%60 +"\n");

                } else {
                    page++;
                    pages.add(new StringBuilder());
                    lines=0;
                }
            }
            if (tips.isEmpty()) {
                pages.get(0).delete(0,pages.get(0).length());
                pages.get(0).append("There are no games to display.");
                return pages;
            } else {
                return pages;
            }
        } catch (SQLException e) {
            System.err.println("no table");
            pages.get(0).delete(0,pages.get(0).length());
            pages.get(0).append("no table");
            return pages;
        }

    }
}



