package com.lanmessenger.server.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for users.
 */
public class UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    private final DatabaseManager dbManager;

    public UserDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void upsertUser(String username) {
        String sql = """
            INSERT INTO users (username, last_seen)
            VALUES (?, ?)
            ON CONFLICT(username) DO UPDATE SET last_seen = excluded.last_seen;
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error upserting user {}", username, e);
        }
    }

    public void updateLastSeen(String username) {
        String sql = "UPDATE users SET last_seen = ? WHERE username = ? COLLATE NOCASE;";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setString(2, username.trim());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating last_seen for user {}", username, e);
        }
    }

    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT username FROM users ORDER BY username ASC;";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all users", e);
        }
        return users;
    }
}
