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
        String sql = "SELECT id, username, password_hash, role, active FROM users WHERE username = ? LIMIT 1";
        return queryOne(sql, statement -> statement.setString(1, username.trim()), this::mapUser);
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Role role;
        try {
            role = Role.valueOf(resultSet.getString("role"));
        } catch (IllegalArgumentException ex) {
            logger.warn("Ignoring user with invalid role: {}", resultSet.getString("username"));
            return null;
        }
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                role,
                resultSet.getBoolean("active")
        );
    }
}
