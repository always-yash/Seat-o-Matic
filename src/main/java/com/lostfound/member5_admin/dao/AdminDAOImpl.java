package com.lostfound.member5_admin.dao;

import com.lostfound.config.DatabaseConnection;
import com.lostfound.model.AuditLog;
import com.lostfound.model.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC Implementation of AdminDAO.
 */
public class AdminDAOImpl implements AdminDAO {

    private static final Logger LOGGER = Logger.getLogger(AdminDAOImpl.class.getName());

    @Override
    public void logAudit(Long userId, String action, String ipAddress) {
        String sql = "INSERT INTO audit_logs (user_id, action, ip_address) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (userId != null) {
                ps.setLong(1, userId);
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setString(2, action);
            ps.setString(3, ipAddress != null ? ipAddress : "127.0.0.1");
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to record audit log: " + action, e);
        }
    }

    @Override
    public List<AuditLog> getRecentAuditLogs(int limit) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT a.log_id, a.user_id, a.action, a.ip_address, a.created_at, " +
                     "COALESCE(u.name, 'System') AS user_name, u.email AS user_email " +
                     "FROM audit_logs a LEFT JOIN users u ON a.user_id = u.user_id " +
                     "ORDER BY a.created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setLogId(rs.getLong("log_id"));
                    log.setUserId(rs.getLong("user_id"));
                    log.setAction(rs.getString("action"));
                    log.setIpAddress(rs.getString("ip_address"));
                    log.setCreatedAt(rs.getTimestamp("created_at"));
                    log.setUserName(rs.getString("user_name"));
                    log.setUserEmail(rs.getString("user_email"));
                    logs.add(log);
                }
            }
        }
        return logs;
    }

    @Override
    public Map<String, Object> getDashboardStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM users) AS total_users, " +
                     "(SELECT COUNT(*) FROM users WHERE status = 'ACTIVE') AS active_users, " +
                     "(SELECT COUNT(*) FROM users WHERE status = 'BLOCKED') AS blocked_users, " +
                     "(SELECT COUNT(*) FROM items) AS total_items, " +
                     "(SELECT COUNT(*) FROM items WHERE item_type = 'LOST') AS lost_items, " +
                     "(SELECT COUNT(*) FROM items WHERE item_type = 'FOUND') AS found_items, " +
                     "(SELECT COUNT(*) FROM items WHERE status = 'MATCHED') AS matched_items, " +
                     "(SELECT COUNT(*) FROM items WHERE status = 'CLAIMED') AS claimed_items, " +
                     "(SELECT COUNT(*) FROM items WHERE status = 'RETURNED') AS returned_items, " +
                     "(SELECT COUNT(*) FROM claims WHERE status = 'PENDING') AS pending_claims, " +
                     "(SELECT COUNT(*) FROM reports WHERE status = 'OPEN') AS open_reports";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                stats.put("totalUsers", rs.getInt("total_users"));
                stats.put("activeUsers", rs.getInt("active_users"));
                stats.put("blockedUsers", rs.getInt("blocked_users"));
                stats.put("totalItems", rs.getInt("total_items"));
                stats.put("lostItems", rs.getInt("lost_items"));
                stats.put("foundItems", rs.getInt("found_items"));
                stats.put("matchedItems", rs.getInt("matched_items"));
                stats.put("claimedItems", rs.getInt("claimed_items"));
                stats.put("returnedItems", rs.getInt("returned_items"));
                stats.put("pendingClaims", rs.getInt("pending_claims"));
                stats.put("openReports", rs.getInt("open_reports"));
            }
        }
        return stats;
    }

    @Override
    public Map<String, Object> getAnalyticsData() throws SQLException {
        Map<String, Object> analytics = new HashMap<>();

        // 1. Items per Category
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        String catSql = "SELECT c.name, COUNT(i.item_id) AS cnt " +
                        "FROM categories c LEFT JOIN items i ON c.category_id = i.category_id " +
                        "GROUP BY c.category_id, c.name ORDER BY cnt DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(catSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categoryCounts.put(rs.getString("name"), rs.getInt("cnt"));
            }
        }
        analytics.put("categoryCounts", categoryCounts);

        // 2. Status distribution
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        String statSql = "SELECT status, COUNT(*) AS cnt FROM items GROUP BY status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(statSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                statusCounts.put(rs.getString("status"), rs.getInt("cnt"));
            }
        }
        analytics.put("statusCounts", statusCounts);

        // 3. Monthly stats (last 6 months)
        Map<String, Map<String, Integer>> monthlyStats = new LinkedHashMap<>();
        String monthSql = "SELECT DATE_FORMAT(item_date, '%Y-%m') AS ym, item_type, COUNT(*) AS cnt " +
                          "FROM items " +
                          "WHERE item_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                          "GROUP BY ym, item_type ORDER BY ym ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(monthSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ym = rs.getString("ym");
                String type = rs.getString("item_type");
                int count = rs.getInt("cnt");

                monthlyStats.computeIfAbsent(ym, k -> new HashMap<>()).put(type, count);
            }
        }
        analytics.put("monthlyStats", monthlyStats);

        return analytics;
    }

    @Override
    public List<Item> getDisposalCandidates(int daysThreshold) throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.*, c.name AS category_name, u.name AS user_name, u.email AS user_email " +
                     "FROM items i " +
                     "JOIN categories c ON i.category_id = c.category_id " +
                     "JOIN users u ON i.user_id = u.user_id " +
                     "WHERE i.status = 'ACTIVE' AND i.item_date <= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                     "ORDER BY i.item_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, daysThreshold > 0 ? daysThreshold : 60);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Item item = new Item();
                    item.setItemId(rs.getLong("item_id"));
                    item.setUserId(rs.getLong("user_id"));
                    item.setCategoryId(rs.getInt("category_id"));
                    item.setTitle(rs.getString("title"));
                    item.setDescription(rs.getString("description"));
                    item.setItemType(rs.getString("item_type"));
                    item.setLocation(rs.getString("location"));
                    item.setLatitude(rs.getDouble("latitude"));
                    item.setLongitude(rs.getDouble("longitude"));
                    item.setItemDate(rs.getDate("item_date"));
                    item.setImage(rs.getString("image"));
                    item.setStatus(rs.getString("status"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    item.setCategoryName(rs.getString("category_name"));
                    item.setUserName(rs.getString("user_name"));
                    item.setUserEmail(rs.getString("user_email"));
                    items.add(item);
                }
            }
        }
        return items;
    }
}
