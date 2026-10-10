package com.seatomatic.core.auth;

import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.common.security.Role;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao extends BaseDAO {
    public User findByUsername(String username) throws SQLException {
        if (username == null || username.isBlank()) {
            return null;
        }
        String sql = "SELECT id, username, password_hash, salt, role, active "
                + "FROM users WHERE username = ? LIMIT 1";
        return queryOne(sql, statement -> statement.setString(1, username.trim()), this::mapUser);
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Role role;
        try {
            String roleValue = resultSet.getString("role");
            if (roleValue == null || roleValue.isBlank()) {
                logger.warn("Ignoring user with missing role: {}", resultSet.getString("username"));
                return null;
            }
            role = Role.valueOf(roleValue.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            logger.warn("Ignoring user with invalid role: {}", resultSet.getString("username"));
            return null;
        }
        String username = resultSet.getString("username");
        String passwordHash = resultSet.getString("password_hash");
        if (username == null || username.isBlank() || passwordHash == null || passwordHash.isBlank()) {
            logger.warn("Ignoring user with incomplete credentials: {}", username);
            return null;
        }
        return new User(
                resultSet.getLong("id"),
                username,
                username,
                passwordHash,
                resultSet.getString("salt"),
                role,
                resultSet.getBoolean("active")
        );
    }
}
