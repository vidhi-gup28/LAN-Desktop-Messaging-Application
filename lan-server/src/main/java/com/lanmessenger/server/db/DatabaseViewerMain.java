package com.lanmessenger.server.db;

import java.sql.*;
import java.util.Scanner;

/**
 * Command-line Database Inspector and Interactive SQL Console for SQLite.
 * Allows teachers, examiners, and developers to inspect tables and run queries from CMD.
 */
public class DatabaseViewerMain {

    public static void main(String[] args) {
        String dbPath = args.length > 0 ? args[0] : "lan_messenger.db";
        String url = "jdbc:sqlite:" + dbPath;

        System.out.println("=============================================================");
        System.out.println("        LAN MESSAGING APPLICATION - DATABASE VIEWER");
        System.out.println("=============================================================");
        System.out.println("Database Engine : SQLite 3 (Embedded via JDBC Driver)");
        System.out.println("Database File   : " + dbPath);
        System.out.println("JDBC URL        : " + url);
        System.out.println("=============================================================\n");

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] SQLite JDBC Driver not found in classpath.");
        }

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // 1. Show all tables
            System.out.println(">>> [1] TABLES IN THIS DATABASE:");
            try (ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name;")) {
                int count = 1;
                while (rs.next()) {
                    String tableName = rs.getString("name");
                    System.out.println("  " + (count++) + ". " + tableName);
                }
            }

            // 2. Show Users
            System.out.println("\n>>> [2] REGISTERED USERS (Table: users):");
            try (ResultSet rs = stmt.executeQuery("SELECT id, username, created_at FROM users ORDER BY id;")) {
                boolean hasUsers = false;
                System.out.printf("  %-5s | %-20s | %-20s%n", "ID", "USERNAME", "CREATED AT");
                System.out.println("  --------------------------------------------------");
                while (rs.next()) {
                    hasUsers = true;
                    System.out.printf("  %-5d | %-20s | %-20s%n",
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("created_at"));
                }
                if (!hasUsers) {
                    System.out.println("  (No registered users found in database)");
                }
            }

            // 3. Show Messages
            System.out.println("\n>>> [3] STORED MESSAGES & CHAT HISTORY (Table: messages):");
            try (ResultSet rs = stmt.executeQuery("SELECT id, sender, receiver, message_type, content, file_name, datetime(timestamp/1000, 'unixepoch', 'localtime') AS sent_time FROM messages ORDER BY id DESC LIMIT 15;")) {
                boolean hasMsg = false;
                System.out.printf("  %-4s | %-12s | %-12s | %-16s | %-19s | %s%n", "ID", "SENDER", "RECEIVER", "TYPE", "TIME", "CONTENT / ATTACHMENT");
                System.out.println("  ------------------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    hasMsg = true;
                    String content = rs.getString("content");
                    String fileName = rs.getString("file_name");
                    String display = (fileName != null && !fileName.isBlank()) ? ("[Attachment: " + fileName + "] " + (content != null ? content : "")) : (content != null ? content : "");
                    if (display.length() > 35) display = display.substring(0, 32) + "...";

                    System.out.printf("  %-4d | %-12s | %-12s | %-16s | %-19s | %s%n",
                            rs.getInt("id"),
                            rs.getString("sender"),
                            rs.getString("receiver") != null ? rs.getString("receiver") : "ALL",
                            rs.getString("message_type"),
                            rs.getString("sent_time"),
                            display);
                }
                if (!hasMsg) {
                    System.out.println("  (No messages found in database)");
                }
            }

            System.out.println("\n=============================================================");
            System.out.println("INTERACTIVE SQL CONSOLE (Type queries or 'exit' to quit)");
            System.out.println("Examples: 'show tables', 'SELECT * FROM messages;', 'exit'");
            System.out.println("=============================================================");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\nSQL> ");
                if (!scanner.hasNextLine()) break;
                String query = scanner.nextLine().trim();
                if (query.equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")) {
                    System.out.println("Exiting database console. Goodbye!");
                    break;
                }
                if (query.isEmpty()) continue;

                // Handle MySQL 'show tables' alias
                if (query.equalsIgnoreCase("show tables") || query.equalsIgnoreCase("show tables;")) {
                    query = "SELECT name AS 'Table_Name' FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';";
                }

                try (Statement queryStmt = conn.createStatement()) {
                    if (query.toLowerCase().startsWith("select")) {
                        try (ResultSet rs = queryStmt.executeQuery(query)) {
                            ResultSetMetaData meta = rs.getMetaData();
                            int cols = meta.getColumnCount();

                            // Print Header
                            for (int i = 1; i <= cols; i++) {
                                System.out.printf("%-18s | ", meta.getColumnLabel(i));
                            }
                            System.out.println();
                            for (int i = 1; i <= cols; i++) {
                                System.out.print("-------------------+--");
                            }
                            System.out.println();

                            // Print Rows
                            int rows = 0;
                            while (rs.next()) {
                                rows++;
                                for (int i = 1; i <= cols; i++) {
                                    String val = rs.getString(i);
                                    if (val != null && val.length() > 16) val = val.substring(0, 13) + "...";
                                    System.out.printf("%-18s | ", val != null ? val : "NULL");
                                }
                                System.out.println();
                            }
                            System.out.println("(" + rows + " rows returned)");
                        }
                    } else {
                        int affected = queryStmt.executeUpdate(query);
                        System.out.println("Query executed successfully. (" + affected + " rows affected)");
                    }
                } catch (SQLException ex) {
                    System.out.println("[SQL ERROR] " + ex.getMessage());
                }
            }

        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] Could not connect to database: " + e.getMessage());
        }
    }
}
