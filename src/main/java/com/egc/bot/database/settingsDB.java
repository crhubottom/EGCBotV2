package com.egc.bot.database;

import org.checkerframework.checker.units.qual.A;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class settingsDB {
    public static void initialize(){
        ArrayList<String> settings = new ArrayList<>();
        settings.add("frankie");
        settings.add("randReply");
        settings.add("voiceTip");
        PreparedStatement ps;
        try {
            for (String setting : settings) {
                ps = Database.con.prepareStatement("SELECT name FROM settings WHERE name = ?");
                ps.setString(1, setting);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println(setting+" not found, adding");
                    PreparedStatement ps2;
                    ps2 = Database.con.prepareStatement("insert into settings values(?,?)");
                    ps2.setString(1, setting);
                    ps2.setBoolean(2, true);
                    ps2.executeQuery();
                }else{
                    System.out.println(setting+" found");
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static boolean getState(String settingName) {

        StringBuilder ss = new StringBuilder();
        PreparedStatement ps;
        boolean state = false;
        try {
            ps = Database.con.prepareStatement("SELECT state FROM settings WHERE name = ?");
            ps.setString(1, settingName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getBoolean("state");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database.");
        }

        return state;
    }
    public static String toggleState(String settingName) {

        StringBuilder ss = new StringBuilder();
        PreparedStatement ps;
        boolean found = false;
        boolean oldState=false;
        try {
            ps = Database.con.prepareStatement("select name from settings where name=?");
            ps.setString(1, settingName.toLowerCase());
           ResultSet rs = ps.executeQuery();
           if(!rs.next()){
               return "Setting not found.";
           }else{
               oldState=rs.getBoolean("state");
           }

            ps = Database.con.prepareStatement("update settings set state = ? where name = ?");
            ps.setBoolean(1, !oldState);
            ps.setString(2, settingName.toLowerCase());
             ps.executeQuery();
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database.");
            return "db error, please fix me Chase";
        }

        return settingName+" is now "+!oldState;
    }
    public static String toggleAll(){

        StringBuilder ss = new StringBuilder();
        PreparedStatement ps;
        boolean found = false;
        boolean oldState=false;
        StringBuilder out = new StringBuilder();
        try {
            ps = Database.con.prepareStatement("select * from settings");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.append(rs.getString("name")).append(": ").append(rs.getBoolean("state")).append(" -> ").append(!rs.getBoolean("state")).append("\n");
                PreparedStatement ps1 = Database.con.prepareStatement("update settings set state = ? where name=?");
                ps1.setBoolean(1, !oldState);
                ps1.setString(2, rs.getString("name"));
                ps1.executeQuery();
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database.");
            return "db error, please fix me Chase";
        }

        return "All settings toggled.\n"+out;
    }

}