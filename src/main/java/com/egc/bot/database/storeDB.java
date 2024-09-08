package com.egc.bot.database;

import net.dv8tion.jda.api.EmbedBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.*;

import static com.egc.bot.Bot.*;

public class storeDB {
    public EmbedBuilder getStore(){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Item Shop");
        eb.setColor(Color.yellow);
        StringBuilder ss = new StringBuilder();
        PreparedStatement ps;
        try {
            ps = Database.con.prepareStatement("SELECT * FROM store ORDER BY cost desc");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ss.append(rs.getString("name") + ": " + rs.getInt("cost"));
                if(rs.getInt("stock") !=10000){
                    ss.append(" : ").append(rs.getInt("stock") + " left.");
                }
                if(rs.getInt("stock") ==0){
                    ss.append(" : ").append("Out of stock.");
                }
                ss.append("\n");
            }
            eb.setDescription(ss.toString());
            return eb;
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
            ss.append("Error: Something went wrong with the database.");
            eb.setDescription(ss.toString());
            return eb;
        }

    }
    public String buy(String item, long id, int amount){
        PreparedStatement ps;
        try {
            ps = Database.con.prepareStatement("SELECT * FROM store WHERE name = ?");
            ps.setString(1, item);
            ResultSet rs = ps.executeQuery();
            System.out.println(item +": "+amount);

            if(rs.next()){
                System.out.println(rs.getInt("cost")*amount);
                if (inv.checkItem(id, "Gold", rs.getInt("cost")*amount)){
                    inv.DeleteItem(id, "Gold", rs.getInt("cost")*amount);
                    inv.AddItem(id, item, amount);
                    return "Item purchased.";
                }
                return "You do not have enough gold.";
            }
            return "Item not found.";
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
            return "Error: Something went wrong with the database.";

        }
    }
}
