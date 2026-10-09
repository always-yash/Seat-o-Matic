package com.seatomatic.core.auth;

import com.seatomatic.common.security.PasswordUtil;

import java.sql.SQLException;

public class AuthService {
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
        return PasswordUtil.verify(password, user.getPasswordHash()) ? user : null;
    }
}
