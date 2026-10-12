package com.seatomatic.common.security;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsrfTokenManagerTest {

    @Test
    void validatesMatchingToken() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(CsrfTokenManager.SESSION_KEY)).thenReturn("token");

        assertTrue(CsrfTokenManager.isValid(session, "token"));
    }

    @Test
    void rejectsNullSessionAndToken() {
        HttpSession session = mock(HttpSession.class);

        assertFalse(CsrfTokenManager.isValid(null, "token"));
        assertFalse(CsrfTokenManager.isValid(session, null));
    }

    @Test
    void rejectsMissingOrMismatchedToken() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(CsrfTokenManager.SESSION_KEY)).thenReturn("expected");

        assertFalse(CsrfTokenManager.isValid(session, "different"));
    }
}
