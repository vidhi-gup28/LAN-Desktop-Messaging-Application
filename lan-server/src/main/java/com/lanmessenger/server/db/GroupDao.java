package com.lanmessenger.server.db;

import com.lanmessenger.common.model.GroupInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for groups and group memberships.
 */
public class GroupDao {

    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);
    private final DatabaseManager dbManager;

    public GroupDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean createGroup(GroupInfo group) {
        String insertGroupSql = "INSERT INTO groups (id, name, created_by, created_at) VALUES (?, ?, ?, ?);";
        String insertMemberSql = "INSERT OR IGNORE INTO group_members (group_id, username, joined_at) VALUES (?, ?, ?);";

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(insertGroupSql)) {
                    pstmt.setString(1, group.getId());
                    pstmt.setString(2, group.getName());
                    pstmt.setString(3, group.getCreatedBy());
                    pstmt.setLong(4, group.getCreatedAt());
                    pstmt.executeUpdate();
                }

                // Add creator and all members
                try (PreparedStatement pstmt = conn.prepareStatement(insertMemberSql)) {
                    for (String member : group.getMembers()) {
                        pstmt.setString(1, group.getId());
                        pstmt.setString(2, member.trim());
                        pstmt.setLong(3, System.currentTimeMillis());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error creating group {}", group.getName(), e);
            return false;
        }
    }

    public GroupInfo getGroup(String groupId) {
        String sql = "SELECT id, name, created_by, created_at FROM groups WHERE id = ?;";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    GroupInfo group = new GroupInfo();
                    group.setId(rs.getString("id"));
                    group.setName(rs.getString("name"));
                    group.setCreatedBy(rs.getString("created_by"));
                    group.setCreatedAt(rs.getLong("created_at"));
                    group.setMembers(getGroupMembers(groupId));
                    return group;
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching group {}", groupId, e);
        }
        return null;
    }

    public List<String> getGroupMembers(String groupId) {
        List<String> members = new ArrayList<>();
        String sql = "SELECT username FROM group_members WHERE group_id = ? ORDER BY joined_at ASC;";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching members for group {}", groupId, e);
        }
        return members;
    }

    public List<GroupInfo> getUserGroups(String username) {
        List<GroupInfo> groups = new ArrayList<>();
        String sql = """
            SELECT g.id, g.name, g.created_by, g.created_at
            FROM groups g
            INNER JOIN group_members gm ON g.id = gm.group_id
            WHERE gm.username = ? COLLATE NOCASE
            ORDER BY g.created_at ASC;
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    GroupInfo group = new GroupInfo();
                    group.setId(rs.getString("id"));
                    group.setName(rs.getString("name"));
                    group.setCreatedBy(rs.getString("created_by"));
                    group.setCreatedAt(rs.getLong("created_at"));
                    groups.add(group);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching groups for user {}", username, e);
        }

        // Populate members for each group
        for (GroupInfo g : groups) {
            g.setMembers(getGroupMembers(g.getId()));
        }
        return groups;
    }

    public boolean isMember(String groupId, String username) {
        String sql = "SELECT 1 FROM group_members WHERE group_id = ? AND username = ? COLLATE NOCASE;";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            pstmt.setString(2, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking membership in group {} for {}", groupId, username, e);
            return false;
        }
    }
}
