package com.tank2d.masterserver.db;

import java.sql.*;

public class AccountRepository {
    private final Connection conn = Connector.getConnection();

    public boolean register(String username, String password) {
        String sql = "INSERT INTO accounts (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            System.out.println("Registered new user: " + username);
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                System.out.println("Username already exists: " + username);
            } else {
                System.out.println("Register error: " + e.getMessage());
            }
            return false;
        }
    }

    public boolean login(String username, String password) {
        String sql = "SELECT * FROM accounts WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            boolean ok = rs.next();
            System.out.println(ok ? "Login OK for " + username : "Login failed for " + username);
            return ok;
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return false;
        }
    }
}