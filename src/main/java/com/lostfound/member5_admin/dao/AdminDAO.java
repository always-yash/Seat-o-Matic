package com.lostfound.member5_admin.dao;

import com.lostfound.model.AuditLog;
import com.lostfound.model.Item;
import com.lostfound.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Admin Data Access Object Interface.
 * Handles admin operations, dashboard metrics, audit logs, and analytics.
 */
public interface AdminDAO {

    /**
     * Records a system audit log entry.
     */
    void logAudit(Long userId, String action, String ipAddress);

    /**
     * Retrieves recent audit log entries.
     */
    List<AuditLog> getRecentAuditLogs(int limit) throws SQLException;

    /**
     * Retrieves aggregated counts for the admin dashboard.
     */
    Map<String, Object> getDashboardStatistics() throws SQLException;

    /**
     * Retrieves analytics trends for charts (monthly items, categories, status breakdown).
     */
    Map<String, Object> getAnalyticsData() throws SQLException;

    /**
     * Finds items that have remained ACTIVE for more than specified days for disposal review.
     */
    List<Item> getDisposalCandidates(int daysThreshold) throws SQLException;
}
