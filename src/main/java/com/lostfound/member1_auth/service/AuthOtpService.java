package com.lostfound.member1_auth.service;

import com.lostfound.config.AppConfig;
import com.lostfound.service.EmailService;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Enterprise Reusable OTP Service.
 * Implements:
 * - 6-digit cryptographically secure OTP generation (SecureRandom)
 * - Expiration window (5 minutes)
 * - Single-use consumption
 * - 60-second resend cooldown protection
 * - Hourly rate limiting (max 5 per hour)
 * - Max attempt throttling (5 attempts per code)
 * - Previous code invalidation upon new generation
 * - Temporary payload storage (e.g., pending registration records)
 * - Integration with Gmail SMTP EmailService
 */
public class AuthOtpService {

    private static final Logger LOGGER = Logger.getLogger(AuthOtpService.class.getName());
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final AuthOtpService INSTANCE = new AuthOtpService();
    private static final int COOLDOWN_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;

    public enum OtpPurpose {
        REGISTER,
        LOGIN,
        FORGOT_PASSWORD,
        CLAIM_ITEM,
        REPORT_LOST_ITEM,
        EMAIL_VERIFICATION
    }

    public static class OtpRecord {
        private final String code;
        private final long expiryTimeMs;
        private final long createdAt;
        private int attempts;
        private Object payload;

        public OtpRecord(String code, int expiryMinutes, Object payload) {
            this.code = code;
            this.createdAt = System.currentTimeMillis();
            this.expiryTimeMs = this.createdAt + (expiryMinutes * 60L * 1000L);
            this.attempts = 0;
            this.payload = payload;
        }

        public String getCode() {
            return code;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTimeMs;
        }

        public int getAttempts() {
            return attempts;
        }

        public void incrementAttempts() {
            this.attempts++;
        }

        public long getRemainingSeconds() {
            long remaining = (expiryTimeMs - System.currentTimeMillis()) / 1000L;
            return Math.max(0, remaining);
        }

        public long getCooldownRemainingSeconds() {
            long elapsed = (System.currentTimeMillis() - createdAt) / 1000L;
            long remaining = COOLDOWN_SECONDS - elapsed;
            return Math.max(0, remaining);
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public Object getPayload() {
            return payload;
        }

        public void setPayload(Object payload) {
            this.payload = payload;
        }
    }

    // Key format: purpose + ":" + email.toLowerCase()
    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();
    // Hourly request counts: email -> timestamp of window start & count
    private final Map<String, int[]> rateLimitMap = new ConcurrentHashMap<>();

    private AuthOtpService() {
        // Singleton
    }

    public static AuthOtpService getInstance() {
        return INSTANCE;
    }

    private String buildKey(String email, OtpPurpose purpose) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        return purpose.name() + ":" + email.trim().toLowerCase();
    }

    /**
     * Masks email for secure public UI display: vivek.garg@gmail.com -> v****g@gmail.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        String trimmed = email.trim();
        int atIdx = trimmed.indexOf('@');
        if (atIdx <= 1) {
            return trimmed;
        }
        String local = trimmed.substring(0, atIdx);
        String domain = trimmed.substring(atIdx);
        if (local.length() <= 2) {
            return local.charAt(0) + "****" + domain;
        }
        return local.charAt(0) + "****" + local.charAt(local.length() - 1) + domain;
    }

    /**
     * Generates a 6-digit OTP, verifies cooldown & rate-limits, and sends via Gmail SMTP.
     */
    public String generateAndSendOtp(String email, OtpPurpose purpose, int expiryMinutes) {
        return generateAndSendOtp(email, purpose, expiryMinutes, null);
    }

    /**
     * Generates a 6-digit OTP with an attached temporary payload (e.g. pending User object).
     */
    public String generateAndSendOtp(String email, OtpPurpose purpose, int expiryMinutes, Object payload) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address is required.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        String key = buildKey(normalizedEmail, purpose);

        // Check 60-second resend cooldown
        OtpRecord existingRecord = otpStore.get(key);
        if (existingRecord != null && !existingRecord.isExpired()) {
            long cooldownLeft = existingRecord.getCooldownRemainingSeconds();
            if (cooldownLeft > 0) {
                throw new IllegalStateException("Please wait " + cooldownLeft + " seconds before requesting a new verification code.");
            }
        }

        // Check hourly rate limit (Max 5 requests per hour)
        checkRateLimit(normalizedEmail);

        // Generate 6-digit cryptographically secure code
        int randomCode = 100000 + SECURE_RANDOM.nextInt(900000);
        String otpCode = String.valueOf(randomCode);

        // Invalidate old OTP and store new record
        OtpRecord newRecord = new OtpRecord(otpCode, expiryMinutes, payload);
        otpStore.put(key, newRecord);

        // Map OtpPurpose to EmailPurpose
        EmailService.EmailPurpose emailPurpose;
        switch (purpose) {
            case REGISTER:
                emailPurpose = EmailService.EmailPurpose.REGISTER;
                break;
            case FORGOT_PASSWORD:
                emailPurpose = EmailService.EmailPurpose.FORGOT_PASSWORD;
                break;
            case CLAIM_ITEM:
                emailPurpose = EmailService.EmailPurpose.CLAIM_ITEM;
                break;
            case REPORT_LOST_ITEM:
                emailPurpose = EmailService.EmailPurpose.REPORT_LOST_ITEM;
                break;
            case LOGIN:
            case EMAIL_VERIFICATION:
            default:
                emailPurpose = EmailService.EmailPurpose.EMAIL_VERIFICATION;
                break;
        }

        // Dispatch via Centralized Gmail EmailService
        EmailService.getInstance().sendOtpEmail(normalizedEmail, otpCode, emailPurpose);
        LOGGER.info("[OTP GENERATED] Purpose: " + purpose + ", Recipient: " + normalizedEmail + ", Code: " + otpCode);

        return otpCode;
    }

    private void checkRateLimit(String email) {
        long now = System.currentTimeMillis();
        rateLimitMap.compute(email, (k, val) -> {
            if (val == null || (now - val[1]) > 3600_000L) {
                // New 1-hour window
                return new int[]{1, (int) (now / 1000)};
            }
            if (val[0] >= 5) {
                throw new IllegalStateException("Hourly verification request limit reached (max 5 per hour). Please try again later.");
            }
            val[0]++;
            return val;
        });
    }

    /**
     * Verifies the OTP entered by user.
     * Throws descriptive exception on failure, returns true and invalidates on success.
     */
    public boolean verifyOtp(String email, OtpPurpose purpose, String enteredOtp) {
        if (email == null || enteredOtp == null) {
            return false;
        }

        String key = buildKey(email, purpose);
        OtpRecord record = otpStore.get(key);

        if (record == null) {
            LOGGER.warning("[OTP VERIFY FAILED] No active OTP found for " + email + " (" + purpose + ")");
            throw new IllegalArgumentException("No active verification code found or code has already been used. Please request a new code.");
        }

        if (record.isExpired()) {
            otpStore.remove(key);
            LOGGER.warning("[OTP VERIFY FAILED] OTP expired for " + email + " (" + purpose + ")");
            throw new IllegalArgumentException("The verification code has expired. Please request a new code.");
        }

        if (record.getAttempts() >= MAX_ATTEMPTS) {
            otpStore.remove(key);
            LOGGER.warning("[OTP VERIFY FAILED] Maximum attempts exceeded for " + email);
            throw new IllegalStateException("Maximum verification attempts exceeded. Please request a new code.");
        }

        record.incrementAttempts();

        boolean matches = record.getCode().equals(enteredOtp.trim());
        if (matches) {
            // Success: Invalidate immediately (single-use)
            otpStore.remove(key);
            LOGGER.info("[OTP VERIFIED] Success for " + email + " (" + purpose + ")");
            return true;
        }

        int remainingAttempts = MAX_ATTEMPTS - record.getAttempts();
        throw new IllegalArgumentException("Invalid verification code. " + remainingAttempts + " attempts remaining.");
    }

    /**
     * Retrieves the stored payload attached to an active OTP record.
     */
    public Object getPayload(String email, OtpPurpose purpose) {
        if (email == null) return null;
        OtpRecord record = otpStore.get(buildKey(email, purpose));
        return record != null ? record.getPayload() : null;
    }

    /**
     * Retrieves the latest active OTP code for developer/evaluation preview.
     */
    public String getLatestOtpForDev(String email, OtpPurpose purpose) {
        if (email == null) return null;
        OtpRecord record = otpStore.get(buildKey(email, purpose));
        if (record != null && !record.isExpired()) {
            return record.getCode();
        }
        return null;
    }

    /**
     * Clears any active OTP for given email and purpose.
     */
    public void clearOtp(String email, OtpPurpose purpose) {
        if (email != null) {
            otpStore.remove(buildKey(email, purpose));
        }
    }
}
