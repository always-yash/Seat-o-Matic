package com.seatomatic.core.auth;

import com.seatomatic.common.db.BaseDAO;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardStatsDao extends BaseDAO {
    private static final String DASHBOARD_STATS_SQL = """
            SELECT
                (SELECT COUNT(*) FROM students WHERE status = 'ACTIVE') AS active_students,
                (SELECT COUNT(*) FROM exams
                    WHERE exam_date >= CURRENT_DATE AND status <> 'LOCKED') AS upcoming_exams,
                (SELECT COUNT(*) FROM rooms WHERE active = TRUE) AS configured_rooms,
                (SELECT COUNT(*) FROM audit_logs) AS audit_events
            """;

    public DashboardStats load() throws SQLException {
        return queryOne(DASHBOARD_STATS_SQL, null, this::mapStats);
    }

    private DashboardStats mapStats(ResultSet resultSet) throws SQLException {
        return new DashboardStats(
                resultSet.getLong("active_students"),
                resultSet.getLong("upcoming_exams"),
                resultSet.getLong("configured_rooms"),
                resultSet.getLong("audit_events"));
    }
}
