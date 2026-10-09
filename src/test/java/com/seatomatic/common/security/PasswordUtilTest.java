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
}
