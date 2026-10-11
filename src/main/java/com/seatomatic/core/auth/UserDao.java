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
        String roleValue = resultSet.getString("role");
        Role role = Role.fromString(roleValue);
        if (role == null) {
            logger.warn("Ignoring user with invalid or missing role: {}", resultSet.getString("username"));
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
