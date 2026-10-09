package com.seatomatic.core.auth;

import com.seatomatic.common.security.PasswordHasher;
import com.seatomatic.common.security.Role;

public class UserDao {

    public User findByUsername(String username) {
        if (username == null) {
            return null;
        }

        String normalized = username.trim();
        if ("admin".equalsIgnoreCase(normalized)) {
            return new User(
                    1L,
                    "admin",
                    "System Administrator",
                    PasswordHasher.hash("admin123"),
                    Role.ADMIN,
                    true
            );
        }

        return null;
    }
}
