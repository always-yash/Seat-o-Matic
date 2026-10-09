package com.seatomatic.common.security;

public final class PasswordHasher {
    private PasswordHasher() {
    }

    public static String hash(String password) {
        return PasswordUtil.hash(password);
    }

    public static boolean matches(String password, String storedHash) {
        return PasswordUtil.verify(password, storedHash);
    }
}
