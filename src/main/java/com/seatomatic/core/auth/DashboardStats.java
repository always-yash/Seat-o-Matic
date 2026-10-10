package com.seatomatic.core.auth;

public final class DashboardStats {
    private final long activeStudents;
    private final long upcomingExams;
    private final long configuredRooms;
    private final long auditEvents;

    public DashboardStats(long activeStudents, long upcomingExams, long configuredRooms, long auditEvents) {
        this.activeStudents = activeStudents;
        this.upcomingExams = upcomingExams;
        this.configuredRooms = configuredRooms;
        this.auditEvents = auditEvents;
    }

    public long getActiveStudents() {
        return activeStudents;
    }

    public long getUpcomingExams() {
        return upcomingExams;
    }

    public long getConfiguredRooms() {
        return configuredRooms;
    }

    public long getAuditEvents() {
        return auditEvents;
    }
}
