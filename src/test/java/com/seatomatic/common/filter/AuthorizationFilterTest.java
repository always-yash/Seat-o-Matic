package com.seatomatic.common.filter;

import com.seatomatic.common.security.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationFilterTest {
    @Test
    void malformedRoleIsRejectedWithoutThrowing() {
        assertNull(AuthorizationFilter.parseRole(null));
        assertNull(AuthorizationFilter.parseRole(""));
        assertNull(AuthorizationFilter.parseRole("not-a-role"));
    }

    @Test
    void validRoleIsParsedCaseInsensitively() {
        assertEquals(Role.ADMIN, AuthorizationFilter.parseRole("admin"));
        assertEquals(Role.FACULTY, AuthorizationFilter.parseRole("FACULTY"));
    }
}
