package com.lostfound.member4_claims.service;

import java.security.SecureRandom;
import java.util.logging.Logger;

/**
 * OTP Service Interface and Default Implementation.
 * Provides cryptographically secure 6-digit OTP generation
 * and delivery abstraction.
 */
public class OtpService {

    private static final Logger LOGGER = Logger.getLogger(OtpService.class.getName());
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a 6-digit numeric OTP.
     */
    public String generateOtp() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Dispatches OTP to user via SMS/Email (Console logging for development environment).
     */
    public void sendOtp(String recipientContact, String otp, String itemTitle) {
        // Log to console for development verification
        System.out.println("=================================================");
        System.out.println(" [DEV OTP NOTIFICATION] To: " + recipientContact);
        System.out.println(" Verification OTP for [" + itemTitle + "]: " + otp);
        System.out.println(" Valid for: 5 minutes");
        System.out.println("=================================================");

        LOGGER.info("[OTP DELIVERED] Recipient: " + recipientContact + ", Code: " + otp);
    }
}
