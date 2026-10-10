package com.seatomatic.core.auth;

public record DashboardStats(
        long activeStudents,
        long upcomingExams,
        long configuredRooms,
        long auditEvents) {
}
