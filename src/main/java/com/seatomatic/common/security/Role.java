package com.seatomatic.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;

/**
 * System roles defining access tiers within Seat-o-Matic.
 */
public enum Role {
    ADMIN,
    FACULTY,
    INVIGILATOR;

    private static final Logger LOGGER = LoggerFactory.getLogger(Role.class);

    /**
     * Safely parses a string into a Role without throwing IllegalArgumentException.
     *
     * @param roleName the string representation of the role
     * @return the resolved Role, or null if null/empty/unrecognized
     */
    public static Role fromString(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return null;
        }
        String normalized = roleName.trim().toUpperCase(Locale.ROOT);
        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            LOGGER.debug("Unrecognized role string: '{}'", roleName);
            return null;
        }
    }

    /**
     * Safely returns an Optional with the parsed Role.
     */
    public static Optional<Role> findRole(String roleName) {
        return Optional.ofNullable(fromString(roleName));
    }

    /**
     * Checks if this role matches the given string role name safely.
     */
    public boolean matches(String roleName) {
        return this == fromString(roleName);
    }
}
