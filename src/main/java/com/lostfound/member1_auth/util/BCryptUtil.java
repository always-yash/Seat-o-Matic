package com.lostfound.member1_auth.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for BCrypt Password Hashing and Verification.
 */
public class BCryptUtil {

    private static final int LOG_ROUNDS = 12;

    private BCryptUtil() {
        // Utility class
    }

    /**
     * Hashes a plaintext password using BCrypt with salt rounds = 12.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty for hashing");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper runner to output verified hashes for development seed accounts.
     */
    public static void main(String[] args) {
        System.out.println("Admin@123 hash: " + hashPassword("Admin@123"));
        System.out.println("Student@123 hash: " + hashPassword("Student@123"));
        System.out.println("Faculty@123 hash: " + hashPassword("Faculty@123"));
    }
}
