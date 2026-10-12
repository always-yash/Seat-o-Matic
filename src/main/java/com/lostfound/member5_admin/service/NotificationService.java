package com.lostfound.member5_admin.service;

import com.lostfound.config.DatabaseConnection;
import com.lostfound.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Notification Service.
 * Delivers alerts, match recommendations, and status updates to users.
 */
public class NotificationService {

    public List<Notification> getNotificationsForUser(Long userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        if (userId == null) return list;

        String sql = "SELECT notification_id, user_id, title, message, is_read, created_at " +
                     "FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                            rs.getLong("notification_id"),
                            rs.getLong("user_id"),
                            rs.getString("title"),
                            rs.getString("message"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return list;
    }

    public int countUnreadNotifications(Long userId) throws SQLException {
        if (userId == null) return 0;
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean markAsRead(Long notificationId) throws SQLException {
        return markAsRead(notificationId, null);
    }

    public boolean markAsRead(Long notificationId, Long userId) throws SQLException {
        if (notificationId == null) return false;
        String sql = (userId != null)
                ? "UPDATE notifications SET is_read = TRUE WHERE notification_id = ? AND user_id = ?"
                : "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, notificationId);
            if (userId != null) {
                ps.setLong(2, userId);
            }
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markAllAsRead(Long userId) throws SQLException {
        if (userId == null) return false;
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public void createNotification(Long userId, String title, String message) {
        if (userId == null || title == null) return;
        String sql = "INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, title);
            ps.setString(3, message != null ? message : "");
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
