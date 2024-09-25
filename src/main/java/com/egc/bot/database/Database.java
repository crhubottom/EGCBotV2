package com.egc.bot.database;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static com.egc.bot.Bot.keys;

public class Database {
    public static Connection con;
    public Database() {
        String user = keys.get("DB_USER");
        String pw = keys.get("DB_PW");

        try {
            con = DriverManager.getConnection(keys.get("DB_URL"), user, pw);
            initialize();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(3);

        }
    }
    private void initialize() throws SQLException {
        Statement statement = con.createStatement();
        statement.execute("CREATE DATABASE IF NOT EXISTS egcDB DEFAULT CHARACTER SET = 'utf8mb4';");
        statement.execute("USE egcDB;");
        statement.execute(
                "CREATE TABLE IF NOT EXISTS tips(id int NOT NULL AUTO_INCREMENT, game varchar(15) NOT NULL , tips varchar(400) NOT NULL ,username varchar(20) NOT NULL ,PRIMARY KEY (id));");
        statement.execute(
                "CREATE TABLE IF NOT EXISTS games(game varchar(150) NOT NULL,timePlayed int default 0,PRIMARY KEY (game));");
        statement.execute(
                "CREATE TABLE IF NOT EXISTS store(name varchar(100) NOT NULL,cost int default 0,stock int default 10000,PRIMARY KEY (name));");
        statement.execute(
                "CREATE TABLE IF NOT EXISTS users(id BIGINT NOT NULL ,date datetime,PRIMARY KEY (id));");
        statement.execute(
                "CREATE TABLE IF NOT EXISTS settings(name varchar(100) ,state boolean, PRIMARY KEY (name));");
        statement.execute(
                "CREATE TABLE IF NOT EXISTS inventory(num int NOT NULL AUTO_INCREMENT,id BIGINT, item varchar(100),count int, PRIMARY KEY (num));");
        statement.execute(
                "CREATE TABLE IF NOT EXISTS messages(id BIGINT, count BIGINT,PRIMARY KEY (id));");
        statement.execute(
                "CREATE TABLE IF NOT EXISTS launches(mission varchar(150) NOT NULL,LSP varchar(100), pad varchar(100),location varchar(100),net varchar(100),image varchar(200),`precision` varchar(150), primary key (mission));");
    }

}

