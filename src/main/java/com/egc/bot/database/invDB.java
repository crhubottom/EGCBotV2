package com.egc.bot.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static com.egc.bot.Bot.*;

public class invDB {
    public void AddItem(Long id, String item, int count) throws SQLException {
        PreparedStatement ps;
        ps = Database.con.prepareStatement("SELECT * FROM inventory WHERE id = ?",ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        System.out.println("Result set made");
        if (!isMyResultSetEmpty(rs)){
            System.out.println("Result set not empty");
            while (rs.next()) {

                if (rs.getString("item")!=null&&rs.getString("item").equals(item)) {
                    System.out.println("Found " + rs.getString("item") + " in " + client.getGuildById(guildID).getMemberById(id).getEffectiveName());
                    String query = "UPDATE inventory SET count=? WHERE id=? AND item=?";
                    PreparedStatement preparedStmt = Database.con.prepareStatement(query);
                    preparedStmt.setInt(1, (rs.getInt("count") + count));
                    preparedStmt.setLong(2, id);
                    preparedStmt.setString(3, item);
                    preparedStmt.execute(); //removes tip
                    return;
                }
            }
        }
        //System.out.println("Did not find "+rs.getString("item")+" in "+client.getGuildById(guildID).getMemberById(id).getEffectiveName());
        String query = "INSERT INTO inventory (id,item,count) VALUES (?, ?,?)";
            PreparedStatement preparedStmt = Database.con.prepareStatement(query);
            preparedStmt.setLong(1, id);
            preparedStmt.setString(2, item);
            preparedStmt.setInt(3, count);
            preparedStmt.execute();
    }
    public static boolean isMyResultSetEmpty(ResultSet rs) throws SQLException {
        return (!rs.isBeforeFirst() && rs.getRow() == 0);
    }
    public void DeleteItem(Long id,String item,int count) throws SQLException {
        PreparedStatement ps;
        System.out.println("Delete item");
        try {
            ps = Database.con.prepareStatement("SELECT * FROM inventory WHERE id = ?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if(rs.getString("item").equals(item)) {
                    System.out.println("Remove item");
                    if((rs.getInt("count")-count)==0) {
                        String query = "DELETE FROM inventory WHERE id=? AND item=?";
                        PreparedStatement preparedStmt = Database.con.prepareStatement(query);
                        preparedStmt.setLong(1, id);
                        preparedStmt.setString(2, item);
                        preparedStmt.execute(); //removes tip
                    }else{
                        System.out.println("Update item");
                        String query = "UPDATE inventory SET count=? WHERE id=? AND item=?";
                        PreparedStatement preparedStmt = Database.con.prepareStatement(query);
                        preparedStmt.setInt(1, (rs.getInt("count")-count));
                        preparedStmt.setLong(2, id);
                        preparedStmt.setString(3, item);
                        preparedStmt.execute(); //removes tip
                    }
                }

            }
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
        }

    }
    public boolean trade(Long initID,Long receiverID,String initItem,String receiverItem,int initCount,int receiverCount) throws SQLException {
        if(initCount!=0&&receiverCount!=0) {
            if (checkItem(initID, initItem, initCount)) {
                if (checkItem(receiverID, receiverItem, receiverCount)) {
                    AddItem(initID, receiverItem, receiverCount);
                    AddItem(receiverID, initItem, initCount);
                    DeleteItem(initID, initItem, initCount);
                    DeleteItem(receiverID, receiverItem, receiverCount);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }else if(initCount==0){
            if (checkItem(receiverID, receiverItem, receiverCount)) {
                AddItem(initID, receiverItem, receiverCount);
                DeleteItem(receiverID, receiverItem, receiverCount);
                return true;
            } else {
                return false;
            }
        }else {
            if (checkItem(initID, initItem, initCount)) {
                AddItem(receiverID, initItem, initCount);
                DeleteItem(initID, initItem, initCount);
                return true;
            }else {
                return false;
            }
        }
    }
    public StringBuilder listInv(Long id){
        StringBuilder list = new StringBuilder();
        PreparedStatement ps;
        try {
            ps = Database.con.prepareStatement("SELECT * FROM inventory where id= ? ORDER BY count");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.append(rs.getString("item")).append(": ").append(rs.getInt("count")).append("\n");
            }
            if (list.isEmpty()) {
                list.append("Inventory is empty.");
            }
            return list;
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
            list.append("Error: Something went wrong with the database.");
            return list;
        }

    }
    public int getGold(Long id) throws SQLException {
        PreparedStatement ps;
        ps = Database.con.prepareStatement("SELECT * FROM inventory WHERE id = ?");
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            if (rs.getString("item").equals("Gold")) {
                // System.out.println(rs.getInt("count"));
                return rs.getInt("count");
            }
        }
        return 0;
    }
    public StringBuilder scoreboard(){
        StringBuilder list = new StringBuilder();
        PreparedStatement ps;
        int count=1;
        try {
            ps = Database.con.prepareStatement("SELECT * FROM inventory ORDER BY count desc");
            ResultSet rs = ps.executeQuery();
            while (rs.next()&&count<4) {
                System.out.println(client.getGuildById(guildID).getMemberById(rs.getLong("id")).getEffectiveName());
                list.append(count).append(": ").append(client.getGuildById(guildID).getMemberById(rs.getLong("id")).getNickname()).append("      ").append(rs.getInt("count")).append("\n");
                count++;
            }

            return list;
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
            list.append("Error: Something went wrong with the database.");
            return list;
        }

    }
    public StringBuilder topGold(){
        StringBuilder list = new StringBuilder();
        PreparedStatement ps;
        try {
            ps = Database.con.prepareStatement("SELECT * FROM inventory ORDER BY count desc LIMIT 1");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.append(client.getGuildById(guildID).getMemberById(rs.getLong("id")).getNickname()).append(" with ").append(rs.getInt("count")).append(" gold.");
            }

            return list;
        } catch (SQLException e) {
            System.err.println("ERROR: Something went wrong with the database. See below for more info.");
            e.printStackTrace();
            list.append("Error: Something went wrong with the database.");
            return list;
        }

    }
    public boolean checkItem(Long id,String item,int count) throws SQLException {
        PreparedStatement ps;
        ps = Database.con.prepareStatement("SELECT * FROM inventory WHERE id = ?");
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            System.out.println(client.getGuildById(guildID).getMemberById(id).getNickname()+":"+item+":"+count+"\n");
            System.out.println(rs.getString("item"));
            if (rs.getString("item").equals(item)) {
               // System.out.println(rs.getInt("count"));
                System.out.println("Found item");
                return (rs.getInt("count") >= count);
            }
        }
        return false;
    }
    public boolean addUser(Long id) throws SQLException {
        PreparedStatement ps;
        java.util.Date dt = new java.util.Date();

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String currentTime = sdf.format(dt);
        ps = Database.con.prepareStatement("SELECT * FROM users WHERE id = ?");
        ps.setLong(1, id);

        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            String query = "UPDATE users SET date=? WHERE id=?";
            PreparedStatement preparedStmt = Database.con.prepareStatement(query);
            preparedStmt.setLong(2, id);
            preparedStmt.setString(1, currentTime);
            preparedStmt.execute();
            return true;
        }
        String query = "INSERT INTO users (id,date) VALUES (?, ?)";
        PreparedStatement preparedStmt = Database.con.prepareStatement(query);
        preparedStmt.setLong(1, id);
        preparedStmt.setString(2, currentTime);
        preparedStmt.execute();
        return true;
    }
    public void purgeUsers() throws SQLException, ParseException {
        PreparedStatement ps;
        ps = Database.con.prepareStatement("SELECT * FROM users");
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(rs.getString("date"), myFormatObj);
            Duration duration = Duration.between(now, dateTime);
            if(duration.toHours()<=-24) {
                System.out.println("Deleting user");
                String query = "DELETE FROM users where id=?";
                PreparedStatement preparedStmt = Database.con.prepareStatement(query);
                preparedStmt.setLong(1, rs.getLong("id"));
                preparedStmt.execute();
            }
        }
    }
    public static void addSalary() throws SQLException {
        PreparedStatement ps;
        ps = Database.con.prepareStatement("SELECT * FROM users");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            try {
               // System.out.println(client.getGuildById(guildID).getMemberById(rs.getLong("id")).getEffectiveName());
                inv.AddItem(rs.getLong("id"), "Gold", 500);
            } catch (SQLException e) {
                System.out.println(e);
            }
        }

    }

}
