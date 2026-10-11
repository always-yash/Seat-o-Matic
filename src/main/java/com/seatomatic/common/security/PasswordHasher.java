package com.seatomatic.common.security;

public final class PasswordHasher {
    private PasswordHasher() {
    }

    public static String hash(String password) {
        return PasswordUtil.hash(password);
    }

    public static String hashBCrypt(String password) {
        return PasswordUtil.hashBCrypt(password);
    }

    public static String hashPBKDF2(String password) {
        return PasswordUtil.hashPBKDF2(password);
    }

    public static boolean matches(String password, String storedHash) {
        return PasswordUtil.verify(password, storedHash);
    }
}
