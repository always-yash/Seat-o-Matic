package com.seatomatic.core.auth;

import com.seatomatic.common.security.PasswordHasher;

public class AuthService {

    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        User user = userDao.findByUsername(username);
        if (user == null || !user.isActive()) {
            return null;
        }

        if (!PasswordHasher.matches(password, user.getPasswordHash())) {
            return null;
        }

        return user;
    }
}
