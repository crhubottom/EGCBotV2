package com.egc.bot.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.egc.bot.Bot.AIc;


public class tipDB {
    private final String game;
    private final String tip;
    private final String username;
    public tipDB(String game, String tip, String username) {
        this.game = game;
        this.tip = tip;
        this.username = username;
    }


    public static boolean addTip(String game, String tip, String username) {
        try {
            String query = "insert into tips (game,tips, username) values (?, ?, ?)";
            PreparedStatement preparedStmt = Database.con.prepareStatement(query);

            preparedStmt.setString(1, game);
            preparedStmt.setString(2, tip);
            preparedStmt.setString(3, username);
            preparedStmt.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
        }
        return false;
    }


    public static boolean removeTip(int id) {
        PreparedStatement ps;
        try {
            ps = Database.con.prepareStatement("SELECT id FROM tips WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String query = "DELETE FROM tips WHERE id=?";
                PreparedStatement preparedStmt = Database.con.prepareStatement(query);
                preparedStmt.setInt(1, id);
                preparedStmt.execute(); //removes tip
                return true;
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

        try {
            ps = Database.con.prepareStatement("SELECT * FROM tips ORDER BY game");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (pages.get(page).length() < 1800) {
                    System.out.println(rs.getInt("id"));
                    pages.get(page).append(rs.getInt("id") + ": " + rs.getString("game") + ": " + rs.getString("tips") + " | " + rs.getString("username") + "\n");
                } else {
                    page++;
                    pages.add(new StringBuilder());
                }
            }
            if (tips.isEmpty()) {
                pages.get(0).append("There are no tips to display.");
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

    public static StringBuilder getCount() {
        StringBuilder ss = new StringBuilder();
        PreparedStatement ps;
        ss.append("**Game Tip Counter**\n\n");
        try {
            ps = Database.con.prepareStatement("SELECT DISTINCT username FROM tips", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(rs.getString("username"));
                try {
                    ps = Database.con.prepareStatement("SELECT tips FROM tips where username =(?)", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    ps.setString(1, rs.getString("username"));
                    ResultSet count = ps.executeQuery();
                    count.last();
                    int rows = count.getRow();
                    System.out.println(rows);
                    if (rows == 1) {
                        ss.append(rs.getString("username")).append(" has added ").append(rows).append(" tip.\n");
                    } else {
                        ss.append(rs.getString("username")).append(" has added ").append(rows).append(" tips.\n");
                    }
                } catch (SQLException e) {
                    System.err.println("ERROR: Something went wrong with the database.");
                    e.printStackTrace();
                    ss.append("Error: Something went wrong with the database. \n");
                    return ss;
                }

            }
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database.");
            e.printStackTrace();
            ss.append("Error: Something went wrong with the database. \n");
            return ss;
        }


        return ss;
    }

    public static String getRandomTip(String game) {
        int ran1 = (int) (Math.random() * 2);
        if (ran1 == 1) {
            PreparedStatement ps;
            try {
                ps = Database.con.prepareStatement("SELECT * FROM tips where game = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ps.setString(1, game);
                ResultSet rs = ps.executeQuery();
                rs.last();
                int rows = rs.getRow();
                System.out.println(rows);
                if (rows == 0) {
                    return AIc.gptCall("give me a short funny or intentionally bad and wrong tip about "+game+". Say nothing but the tip.","gpt-4o-mini");
                }
                rs.first();
                int ran = (int) (Math.random() * rows);
                System.out.println(ran);
                rs.absolute(ran + 1);
                return rs.getString("tips");

            } catch (SQLException e) {
                System.err.println("ERROR: Something went wrong with the database. See below for more info.");
                e.printStackTrace();
                return "error, plz fix";
            }

        }else{
            return AIc.gptCall("give me a short funny or intentionally bad and wrong tip about "+game+". Say nothing but the tip.","gpt-4o-mini");
        }

    }
}



