package com.seatomatic.common.security;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cryptographic utility providing secure password hashing and verification.
 * Supports both standard industry BCrypt and PBKDF2WithHmacSHA256,
 * with constant-time verification and strict fail-closed security.
 */
public final class PasswordUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordUtil.class);
    private static final String ALLOW_PLAINTEXT_PROPERTY = "seatomatic.auth.allowPlaintextPasswords";
    private static final int BCRYPT_LOG_ROUNDS = 12;
    private static final int ITERATIONS = 600_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Hashes a password using PBKDF2WithHmacSHA256 with a cryptographically secure salt.
     *
     * @param password the plaintext password to hash
     * @return salt:hash encoded string
     */
    public static String hash(String password) {
        return hashPBKDF2(password);
    }

    /**
     * Explicitly hashes a password using PBKDF2WithHmacSHA256.
     */
    public static String hashPBKDF2(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return encode(salt) + ":" + encode(derive(password, salt));
    }

    /**
     * Hashes a password using BCrypt with salt rounds configured to 12.
     *
     * @param password the plaintext password to hash
     * @return standard BCrypt hash string
     */
    public static String hashBCrypt(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    /**
     * Verifies a candidate plaintext password against a stored hash.
     * Supports both BCrypt hashes (starting with $2a$, $2b$, or $2y$)
     * and PBKDF2 hashes (formatted as salt:hash), with fail-closed semantics.
     *
     * @param password   the plaintext password candidate
     * @param storedHash the persisted hash string
     * @return true if candidate matches stored hash, false otherwise
     */
    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        if (isBcryptHash(storedHash)) {
            try {
                return BCrypt.checkpw(password, storedHash);
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("Rejected malformed BCrypt password hash", ex);
                return false;
            }
        }
        if (isPlaintextFallbackEnabled() && MessageDigest.isEqual(
                password.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            LOGGER.warn("Accepted a plaintext password because {} is enabled; disable it outside local testing",
                    ALLOW_PLAINTEXT_PROPERTY);
            return true;
        }
        String[] parts = storedHash.split(":", -1);
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            return MessageDigest.isEqual(expected, derive(password, salt));
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Rejected malformed PBKDF2 password hash");
            return false;
        }
    }

    private static boolean isBcryptHash(String storedHash) {
        return storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$");
    }

    private static boolean isPlaintextFallbackEnabled() {
        return Boolean.getBoolean(ALLOW_PLAINTEXT_PROPERTY);
    }

    private static byte[] derive(String password, byte[] salt) {
        try {
            PBEKeySpec specification = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(specification).getEncoded();
            } finally {
                specification.clearPassword();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to derive password hash", ex);
        }
    }

    private static String encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }
}
