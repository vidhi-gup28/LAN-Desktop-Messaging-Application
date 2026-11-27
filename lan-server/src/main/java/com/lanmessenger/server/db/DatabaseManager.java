package com.lanmessenger.server.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and schema initialization.
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final String dbUrl;

    public DatabaseManager(String dbFilePath) {
        // Ensure parent directory exists if specified
        File dbFile = new File(dbFilePath);
        if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }
        this.dbUrl = "jdbc:sqlite:" + dbFilePath;
        initializeDatabase();
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        // Enable WAL mode for high concurrency
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA synchronous = NORMAL;");
        }
        return conn;
    }

    private void initializeDatabase() {
        logger.info("Initializing SQLite database at {}", dbUrl);
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Users table
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT UNIQUE NOT NULL COLLATE NOCASE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            last_seen INTEGER NOT NULL
                        );
                    """);

            // 2. Groups table
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS groups (
                            id TEXT PRIMARY KEY,
                            name TEXT NOT NULL,
                            created_by TEXT NOT NULL,
                            created_at INTEGER NOT NULL
                        );
                    """);

            // 3. Group members table
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS group_members (
                            group_id TEXT NOT NULL,
                            username TEXT NOT NULL COLLATE NOCASE,
                            joined_at INTEGER NOT NULL,
                            PRIMARY KEY (group_id, username),
                            FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
                        );
                    """);

            // 4. Messages table
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS messages (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            sender TEXT NOT NULL COLLATE NOCASE,
                            receiver TEXT COLLATE NOCASE,
                            group_id TEXT,
                            message_type TEXT NOT NULL,
                            content TEXT NOT NULL,
                            timestamp INTEGER NOT NULL,
                            file_name TEXT,
                            file_size INTEGER,
                            file_data TEXT
                        );
                    """);

            // Migration check: add columns if table was created in an earlier version
            try (Statement alterStmt = conn.createStatement()) {
                alterStmt.execute("ALTER TABLE messages ADD COLUMN file_name TEXT;");
            } catch (SQLException ignored) {
            }
            try (Statement alterStmt = conn.createStatement()) {
                alterStmt.execute("ALTER TABLE messages ADD COLUMN file_size INTEGER;");
            } catch (SQLException ignored) {
            }
            try (Statement alterStmt = conn.createStatement()) {
                alterStmt.execute("ALTER TABLE messages ADD COLUMN file_data TEXT;");
            } catch (SQLException ignored) {
            }

            // Indexes for fast chat history retrieval
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_private ON messages(sender, receiver, timestamp);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_group ON messages(group_id, timestamp);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_type ON messages(message_type, timestamp);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_group_members_user ON group_members(username);");

            logger.info("Database schema verified and indexes established.");
        } catch (SQLException e) {
            logger.error("Failed to initialize SQLite database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Clears all chat history, groups, memberships, and registered users from the
     * database.
     */
    public void clearAllData() {
        logger.info("Clearing all data from SQLite database...");
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM messages;");
            stmt.execute("DELETE FROM group_members;");
            stmt.execute("DELETE FROM groups;");
            stmt.execute("DELETE FROM users;");
            stmt.execute("VACUUM;");
            logger.info("All history, groups, and users cleared successfully.");
        } catch (SQLException e) {
            logger.error("Failed to clear database data", e);
            throw new RuntimeException("Clear database failed", e);
        }
    }
}
