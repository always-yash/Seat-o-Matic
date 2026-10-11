package com.seatomatic.common.security;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testFromStringValid() {
        assertEquals(Role.ADMIN, Role.fromString("ADMIN"));
        assertEquals(Role.ADMIN, Role.fromString("admin"));
        assertEquals(Role.ADMIN, Role.fromString(" Admin "));
        assertEquals(Role.FACULTY, Role.fromString("faculty"));
        assertEquals(Role.INVIGILATOR, Role.fromString("invigilator"));
    }

    @Test
    void testFromStringInvalidOrNullDoesNotThrow() {
        assertNull(Role.fromString(null));
        assertNull(Role.fromString(""));
        assertNull(Role.fromString("   "));
        assertNull(Role.fromString("UNKNOWN_ROLE"));
        assertNull(Role.fromString("SUPERADMIN"));
    }

    @Test
    void testFindRoleOptional() {
        Optional<Role> roleOpt = Role.findRole("FACULTY");
        assertTrue(roleOpt.isPresent());
        assertEquals(Role.FACULTY, roleOpt.get());

        assertTrue(Role.findRole("invalid").isEmpty());
        assertTrue(Role.findRole(null).isEmpty());
    }

    @Test
    void testMatches() {
        assertTrue(Role.ADMIN.matches("ADMIN"));
        assertTrue(Role.ADMIN.matches("admin"));
        assertFalse(Role.ADMIN.matches("FACULTY"));
        assertFalse(Role.ADMIN.matches(null));
    }
}
