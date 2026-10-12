package com.seatomatic.paper.model;

/**
 * Guarded distribution lifecycle state machine:
 * PENDING -> PREPARED -> DISTRIBUTED -> VERIFIED (Terminal).
 */
public enum DistributionStatus {
    PENDING {
        @Override
        public boolean canTransitionTo(DistributionStatus next) {
            return next == PREPARED;
        }
    },
    PREPARED {
        @Override
        public boolean canTransitionTo(DistributionStatus next) {
            return next == DISTRIBUTED;
        }
    },
    DISTRIBUTED {
        @Override
        public boolean canTransitionTo(DistributionStatus next) {
            return next == VERIFIED;
        }
    },
    VERIFIED {
        @Override
        public boolean canTransitionTo(DistributionStatus next) {
            // VERIFIED is terminal
            return false;
        }
    };

    /**
     * Asserts whether the given target state is legal from this state.
     */
    public abstract boolean canTransitionTo(DistributionStatus next);

    public boolean isTerminal() {
        return this == VERIFIED;
    }

    public static DistributionStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        for (DistributionStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown DistributionStatus: " + value);
    }
}
