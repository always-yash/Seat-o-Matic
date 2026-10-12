package com.seatomatic.common.security;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public final class CsrfTokenManager {

    public static final String SESSION_KEY = "csrfToken";

    private CsrfTokenManager() {
    }

    public static String generateToken(HttpSession session) {
        String token = UUID.randomUUID().toString();
        session.setAttribute(SESSION_KEY, token);
        return token;
    }

    public static String token(HttpSession session) {
        return (String) session.getAttribute(SESSION_KEY);
    }

    public static boolean isValid(HttpSession session, String incomingToken) {
        if (session == null || incomingToken == null) {
            return false;
        }
        String expected = token(session);
        return expected != null && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                incomingToken.getBytes(StandardCharsets.UTF_8)
        );
    }
}
