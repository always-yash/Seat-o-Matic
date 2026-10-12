package com.lostfound.member1_auth.dao;

import com.lostfound.config.DatabaseConnection;
import com.lostfound.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Implementation of UserDAO.
 * Uses PreparedStatements and resilient column mapping.
 */
public class UserDAOImpl implements UserDAO {

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setDepartment(rs.getString("department"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setStatus(rs.getString("status"));

        try {
            user.setStudentId(rs.getString("student_id"));
        } catch (SQLException ignored) {}

        try {
            user.setGoogleId(rs.getString("google_id"));
        } catch (SQLException ignored) {}

        try {
            user.setProfileImage(rs.getString("profile_image"));
        } catch (SQLException ignored) {}

        try {
            user.setEmailVerified(rs.getBoolean("email_verified"));
        } catch (SQLException ignored) {}

        try {
            String provider = rs.getString("auth_provider");
            if (provider != null) user.setAuthProvider(provider);
        } catch (SQLException ignored) {}

        return user;
    }

    @Override
    public User register(User user) throws SQLException {
        String fullSql = "INSERT INTO users (name, email, password, phone, role, department, status, student_id, google_id, profile_image, email_verified, auth_provider) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String legacySql = "INSERT INTO users (name, email, password, phone, role, department, status) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps;
            try {
                ps = conn.prepareStatement(fullSql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getPhone());
                ps.setString(5, user.getRole() != null ? user.getRole() : "STUDENT");
                ps.setString(6, user.getDepartment());
                ps.setString(7, user.getStatus() != null ? user.getStatus() : "ACTIVE");
                ps.setString(8, user.getStudentId());
                ps.setString(9, user.getGoogleId());
                ps.setString(10, user.getProfileImage());
                ps.setBoolean(11, user.isEmailVerified());
                ps.setString(12, user.getAuthProvider() != null ? user.getAuthProvider() : "LOCAL");
            } catch (SQLException e) {
                // Fallback to legacy insert
                ps = conn.prepareStatement(legacySql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getPhone());
                ps.setString(5, user.getRole() != null ? user.getRole() : "STUDENT");
                ps.setString(6, user.getDepartment());
                ps.setString(7, user.getStatus() != null ? user.getStatus() : "ACTIVE");
            }

            try (PreparedStatement statement = ps) {
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
                return user;
            }
        }
    }

    @Override
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email != null ? email.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public User findById(Long userId) throws SQLException {
        if (userId == null) return null;
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public User findByGoogleId(String googleId) throws SQLException {
        if (googleId == null || googleId.isBlank()) return null;
        String sql = "SELECT * FROM users WHERE google_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, googleId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean updateProfile(User user) throws SQLException {
        String fullSql = "UPDATE users SET name = ?, phone = ?, department = ?, student_id = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(fullSql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getDepartment());
            ps.setString(4, user.getStudentId());
            ps.setLong(5, user.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            String legacySql = "UPDATE users SET name = ?, phone = ?, department = ? WHERE user_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(legacySql)) {

                ps.setString(1, user.getName());
                ps.setString(2, user.getPhone());
                ps.setString(3, user.getDepartment());
                ps.setLong(4, user.getUserId());

                return ps.executeUpdate() > 0;
            }
        }
    }

    @Override
    public boolean updatePassword(Long userId, String newHashedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newHashedPassword);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateGoogleOAuth(Long userId, String googleId, String profileImage) throws SQLException {
        String sql = "UPDATE users SET google_id = ?, profile_image = ?, auth_provider = 'GOOGLE', email_verified = TRUE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, googleId);
            ps.setString(2, profileImage);
            ps.setLong(3, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean updateEmailVerified(Long userId, boolean verified) throws SQLException {
        String sql = "UPDATE users SET email_verified = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, verified);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean blockUser(Long userId) throws SQLException {
        String sql = "UPDATE users SET status = 'BLOCKED' WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean unblockUser(Long userId) throws SQLException {
        String sql = "UPDATE users SET status = 'ACTIVE' WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateRole(Long userId, String newRole) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newRole);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    @Override
    public List<User> searchUsers(String keyword) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(department) LIKE ? " +
                     "ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + (keyword != null ? keyword.trim().toLowerCase() : "") + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        return users;
    }

    @Override
    public int countTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int countActiveUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int countBlockedUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE status = 'BLOCKED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int countAdmins() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
