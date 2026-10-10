package com.seatomatic.core.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardStatsTest {
    @Test
    void exposesAllDashboardCounters() {
        DashboardStats stats = new DashboardStats(12, 3, 4, 19);

        assertEquals(12, stats.activeStudents());
        assertEquals(3, stats.upcomingExams());
        assertEquals(4, stats.configuredRooms());
        assertEquals(19, stats.auditEvents());
    }
}
