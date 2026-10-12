package com.lostfound.member5_admin.dao;

import com.lostfound.config.DatabaseConnection;
import com.lostfound.model.Report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Implementation of ReportDAO.
 */
public class ReportDAOImpl implements ReportDAO {

    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getLong("report_id"));
        report.setReportedBy(rs.getLong("reported_by"));
        report.setItemId(rs.getLong("item_id"));
        report.setReason(rs.getString("reason"));
        report.setDescription(rs.getString("description"));
        report.setStatus(rs.getString("status"));
        report.setCreatedAt(rs.getTimestamp("created_at"));

        try { report.setReporterName(rs.getString("reporter_name")); } catch (SQLException ignored) {}
        try { report.setReporterEmail(rs.getString("reporter_email")); } catch (SQLException ignored) {}
        try { report.setItemTitle(rs.getString("item_title")); } catch (SQLException ignored) {}

        return report;
    }

    private static final String BASE_SELECT =
            "SELECT r.report_id, r.reported_by, r.item_id, r.reason, r.description, " +
            "r.status, r.created_at, u.name AS reporter_name, u.email AS reporter_email, " +
            "i.title AS item_title " +
            "FROM reports r " +
            "JOIN users u ON r.reported_by = u.user_id " +
            "LEFT JOIN items i ON r.item_id = i.item_id ";

    @Override
    public Report create(Report report) throws SQLException {
        String sql = "INSERT INTO reports (reported_by, item_id, reason, description, status) " +
                     "VALUES (?, ?, ?, ?, 'OPEN')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, report.getReportedBy());
            if (report.getItemId() != null) {
                ps.setLong(2, report.getItemId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setString(3, report.getReason());
            ps.setString(4, report.getDescription());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    report.setReportId(rs.getLong(1));
                    report.setStatus("OPEN");
                }
            }
            return report;
        }
    }

    @Override
    public Report findById(Long reportId) throws SQLException {
        if (reportId == null) return null;
        String sql = BASE_SELECT + "WHERE r.report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReport(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Report> findAll() throws SQLException {
        List<Report> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY r.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToReport(rs));
            }
        }
        return list;
    }

    @Override
    public List<Report> findByStatus(String status) throws SQLException {
        List<Report> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.status = ? ORDER BY r.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReport(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean updateStatus(Long reportId, String status) throws SQLException {
        String sql = "UPDATE reports SET status = ? WHERE report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setLong(2, reportId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int countOpenReports() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reports WHERE status = 'OPEN'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
