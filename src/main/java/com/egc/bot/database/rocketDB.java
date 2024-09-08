package com.egc.bot.database;


import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class rocketDB {
    public static void updateDB() throws IOException, SQLException {
        PreparedStatement ps;
        String api="https://ll.thespacedevs.com/2.2.0/launch/upcoming/?format=json&limit=30&mode=list";
        JSONObject json;
        try {
         json=new JSONObject((IOUtils.toString(new URL(api), StandardCharsets.UTF_8)));
        }catch (IOException e){
            System.out.println("Rate limit");
            return;

        }
        JSONArray results = new JSONArray(json.getJSONArray("results"));
        String query = "DELETE FROM launches";
        PreparedStatement preparedStmt = Database.con.prepareStatement(query);
        preparedStmt.execute();

        for (int i = 0; i < results.length(); ++i) {
            if (results.getJSONObject(i).getJSONObject("status").getInt("id") == 1||results.getJSONObject(i).getJSONObject("status").getInt("id") == 2||results.getJSONObject(i).getJSONObject("status").getInt("id") == 8) {
                String net = results.getJSONObject(i).getString("net");
              //  String date = net.substring(0, net.lastIndexOf("T"));
               // String time = net.substring(net.lastIndexOf("T") + 1, net.lastIndexOf("Z"));
                String mission=results.getJSONObject(i).getString("name");
                String lsp=results.getJSONObject(i).getString("lsp_name");
                String location=results.getJSONObject(i).getString("location");
                String pad=results.getJSONObject(i).getString("pad");
                String image=results.getJSONObject(i).getString("image");
                String precision=results.getJSONObject(i).getJSONObject("net_precision").getString("description");
                String query2 = "INSERT INTO launches (mission,LSP,pad,location,net,image,`precision`) VALUES (?,?,?,?,?,?,?)";
                PreparedStatement preparedStmt2 = Database.con.prepareStatement(query2);
                preparedStmt2.setString(1, mission);
                preparedStmt2.setString(2, lsp);
                preparedStmt2.setString(3, pad);
                preparedStmt2.setString(4, location);
                preparedStmt2.setString(5, net);
                preparedStmt2.setString(6, image);
                preparedStmt2.setString(7, precision);
                preparedStmt2.execute();
            }

        }

    }



    }




