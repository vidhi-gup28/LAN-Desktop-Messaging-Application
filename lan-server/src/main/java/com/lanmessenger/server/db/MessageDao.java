package com.lanmessenger.server.db;

import com.lanmessenger.common.model.ChatMessage;
import com.lanmessenger.common.model.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for persisting and retrieving chat messages.
 */
public class MessageDao {

    private static final Logger logger = LoggerFactory.getLogger(MessageDao.class);
    private final DatabaseManager dbManager;

    public MessageDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public ChatMessage saveMessage(ChatMessage msg) {
        String sql = """
                    INSERT INTO messages (sender, receiver, group_id, message_type, content, timestamp, file_name, file_size, file_data)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, msg.getSender());
            pstmt.setString(2, msg.getReceiver());
            pstmt.setString(3, msg.getGroupId());
            pstmt.setString(4,
                    msg.getMessageType() != null ? msg.getMessageType().name() : MessageType.PRIVATE_MESSAGE.name());
            pstmt.setString(5, msg.getContent());
            pstmt.setLong(6, msg.getTimestamp() != null ? msg.getTimestamp() : System.currentTimeMillis());
            pstmt.setString(7, msg.getFileName());
            if (msg.getFileSize() != null) {
                pstmt.setLong(8, msg.getFileSize());
            } else {
                pstmt.setNull(8, Types.BIGINT);
            }
            pstmt.setString(9, msg.getFileData());

            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    msg.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving message from {}", msg.getSender(), e);
        }
        return msg;
    }

    public List<ChatMessage> getPrivateHistory(String user1, String user2, int limit) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = """
                    SELECT id, sender, receiver, group_id, message_type, content, timestamp, file_name, file_size, file_data
                    FROM messages
                    WHERE ((sender = ? COLLATE NOCASE AND receiver = ? COLLATE NOCASE)
                       OR  (sender = ? COLLATE NOCASE AND receiver = ? COLLATE NOCASE))
                       AND message_type = 'PRIVATE_MESSAGE'
                    ORDER BY timestamp DESC
                    LIMIT ?;
                """;
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user1.trim());
            pstmt.setString(2, user2.trim());
            pstmt.setString(3, user2.trim());
            pstmt.setString(4, user1.trim());
            pstmt.setInt(5, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching private history between {} and {}", user1, user2, e);
        }
        // Reverse so that oldest is first (chronological order)
        Collections.reverse(list);
        return list;
    }

    public List<ChatMessage> getGroupHistory(String groupId, int limit) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = """
                    SELECT id, sender, receiver, group_id, message_type, content, timestamp, file_name, file_size, file_data
                    FROM messages
                    WHERE group_id = ? AND message_type = 'GROUP_MESSAGE'
                    ORDER BY timestamp DESC
                    LIMIT ?;
                """;
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching group history for group {}", groupId, e);
        }
        Collections.reverse(list);
        return list;
    }

    public List<ChatMessage> getBroadcastHistory(int limit) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = """
                    SELECT id, sender, receiver, group_id, message_type, content, timestamp, file_name, file_size, file_data
                    FROM messages
                    WHERE message_type = 'BROADCAST_MESSAGE'
                    ORDER BY timestamp DESC
                    LIMIT ?;
                """;
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching broadcast history", e);
        }
        Collections.reverse(list);
        return list;
    }

    private ChatMessage mapRow(ResultSet rs) throws SQLException {
        ChatMessage msg = new ChatMessage();
        msg.setId(rs.getLong("id"));
        msg.setSender(rs.getString("sender"));
        msg.setReceiver(rs.getString("receiver"));
        msg.setGroupId(rs.getString("group_id"));
        msg.setMessageType(MessageType.valueOf(rs.getString("message_type")));
        msg.setContent(rs.getString("content"));
        msg.setTimestamp(rs.getLong("timestamp"));
        msg.setFileName(rs.getString("file_name"));
        long size = rs.getLong("file_size");
        if (!rs.wasNull()) {
            msg.setFileSize(size);
        }
        msg.setFileData(rs.getString("file_data"));
        return msg;
    }
}
