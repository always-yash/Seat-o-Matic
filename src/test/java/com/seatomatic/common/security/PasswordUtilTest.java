package com.seatomatic.common.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {
    @Test
    void hashCanBeVerifiedAndDoesNotExposeThePassword() {
        String hash = PasswordUtil.hash("correct horse battery staple");
        assertNotEquals("correct horse battery staple", hash);
        assertTrue(PasswordUtil.verify("correct horse battery staple", hash));
        assertFalse(PasswordUtil.verify("wrong password", hash));
    }

    @Test
    void malformedHashesFailClosed() {
        assertFalse(PasswordUtil.verify("password", null));
        assertFalse(PasswordUtil.verify("password", "not-a-valid-hash"));
    }

    @Test
    void bcryptHashesAreSupported() {
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw("password", org.mindrot.jbcrypt.BCrypt.gensalt());
        assertTrue(PasswordUtil.verify("password", hash));
        assertFalse(PasswordUtil.verify("wrong password", hash));
    }

    @Test
    void plaintextFallbackIsDisabledByDefault() {
        String previous = System.getProperty("seatomatic.auth.allowPlaintextPasswords");
        try {
            System.clearProperty("seatomatic.auth.allowPlaintextPasswords");
            assertFalse(PasswordUtil.verify("admin123", "admin123"));
        } finally {
            restoreProperty(previous);
        }
    }

    @Test
    void plaintextFallbackRequiresExplicitLocalProperty() {
        String previous = System.getProperty("seatomatic.auth.allowPlaintextPasswords");
        try {
            System.setProperty("seatomatic.auth.allowPlaintextPasswords", "true");
            assertTrue(PasswordUtil.verify("admin123", "admin123"));
        } finally {
            restoreProperty(previous);
        }
    }

    private void restoreProperty(String value) {
        if (value == null) {
            System.clearProperty("seatomatic.auth.allowPlaintextPasswords");
        } else {
            System.setProperty("seatomatic.auth.allowPlaintextPasswords", value);
        }
    }
}
