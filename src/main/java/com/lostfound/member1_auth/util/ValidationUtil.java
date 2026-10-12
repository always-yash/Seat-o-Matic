package com.lostfound.member1_auth.util;

import java.util.regex.Pattern;

/**
 * Validation and Sanitization Utility.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+?[0-9]{1,4}[ -]?)?[0-9]{7,15}$"
    );

    private ValidationUtil() {
        // Utility class
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // At least one letter and at least one digit or special char
        boolean hasLetter = false;
        boolean hasDigitOrSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else {
                hasDigitOrSpecial = true;
            }
        }
        return hasLetter && hasDigitOrSpecial;
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Optional field
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Sanitizes strings to prevent basic XSS when rendering or logging.
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    public static String clean(String input) {
        return input == null ? "" : input.trim();
    }

    public static Long parseLong(String val) {
        try {
            if (val != null && !val.trim().isEmpty()) {
                return Long.parseLong(val.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public static Integer parseInt(String val) {
        try {
            if (val != null && !val.trim().isEmpty()) {
                return Integer.parseInt(val.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public static Double parseDouble(String val) {
        try {
            if (val != null && !val.trim().isEmpty()) {
                return Double.parseDouble(val.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
