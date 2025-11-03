package com.tank2d.masterserver.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {
    private static final String URL = "jdbc:mysql://localhost:3306/pixeltank_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver"); // load driver MySQL
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to MySQL database!");
            } catch (SQLException e) {
                System.out.println("Database connection error: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.out.println("MySQL Driver not found!");
            }
        }
        return connection;
    }
}