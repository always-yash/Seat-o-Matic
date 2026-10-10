package com.seatomatic.core.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardStatsTest {
    @Test
    void exposesAllDashboardCounters() {
        DashboardStats stats = new DashboardStats(12, 3, 4, 19);

        assertEquals(12, stats.getActiveStudents());
        assertEquals(3, stats.getUpcomingExams());
        assertEquals(4, stats.getConfiguredRooms());
        assertEquals(19, stats.getAuditEvents());
    }
}
