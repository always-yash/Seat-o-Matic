package com.seatomatic.core.auth;

import com.seatomatic.common.security.PasswordUtil;

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private final UserDao userDao;

    public AuthService() {
        this(new UserDao());
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User authenticate(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return null;
        }
        User user = userDao.findByUsername(username);
        if (user == null || !user.isActive()) {
            return null;
        }
        try {
            return PasswordUtil.verify(password, user.getPasswordHash()) ? user : null;
        } catch (RuntimeException ex) {
            LOGGER.error("Password verification failed for username {}", username.trim(), ex);
            return null;
        }
    }
}
