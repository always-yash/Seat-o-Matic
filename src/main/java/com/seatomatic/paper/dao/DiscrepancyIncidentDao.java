package com.seatomatic.paper.dao;

import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.paper.model.DiscrepancyIncident;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for room discrepancies and examination incidents.
 */
public class DiscrepancyIncidentDao extends BaseDAO {

    private static final String SELECT_BASE =
            "SELECT id, distribution_id, exam_id, room_id, incident_type, count_affected, " +
            "       description, reported_by, status, created_at, resolved_at " +
            "FROM paper_discrepancies ";

    public Long insert(DiscrepancyIncident incident) throws SQLException {
        if (incident == null) {
            throw new IllegalArgumentException("Discrepancy incident must not be null");
        }
        String sql = "INSERT INTO paper_discrepancies (distribution_id, exam_id, room_id, incident_type, " +
                     "                               count_affected, description, reported_by, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Long id = executeInsert(sql, incident.getDistributionId(), incident.getExamId(), incident.getRoomId(),
                incident.getIncidentType(), incident.getCountAffected(), incident.getDescription(),
                incident.getReportedBy(), incident.getStatus() != null ? incident.getStatus() : "REPORTED");
        if (id != null) {
            incident.setId(id);
        }
        return id;
    }

    public Optional<DiscrepancyIncident> findById(Long id) throws SQLException {
        if (id == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE id = ?";
        return queryOptional(sql, this::mapRow, id);
    }

    public List<DiscrepancyIncident> findByExamId(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE exam_id = ? ORDER BY id DESC";
        return queryList(sql, this::mapRow, examId);
    }

    public List<DiscrepancyIncident> findByRoomId(Long roomId) throws SQLException {
        if (roomId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE room_id = ? ORDER BY id DESC";
        return queryList(sql, this::mapRow, roomId);
    }

    public int updateStatus(Long id, String status) throws SQLException {
        if (id == null || status == null || status.isBlank()) {
            return 0;
        }
        String sql = "UPDATE paper_discrepancies SET status = ?, " +
                     "resolved_at = CASE WHEN ? = 'RESOLVED' THEN CURRENT_TIMESTAMP ELSE resolved_at END " +
                     "WHERE id = ?";
        return executeUpdate(sql, status.trim().toUpperCase(), status.trim().toUpperCase(), id);
    }

    private DiscrepancyIncident mapRow(ResultSet rs) throws SQLException {
        DiscrepancyIncident inc = new DiscrepancyIncident();
        inc.setId(rs.getLong("id"));
        inc.setDistributionId(rs.getLong("distribution_id"));
        inc.setExamId(rs.getLong("exam_id"));
        inc.setRoomId(rs.getLong("room_id"));
        inc.setIncidentType(rs.getString("incident_type"));
        inc.setCountAffected(rs.getInt("count_affected"));
        inc.setDescription(rs.getString("description"));
        inc.setReportedBy(rs.getLong("reported_by"));
        inc.setStatus(rs.getString("status"));
        inc.setCreatedAt(rs.getTimestamp("created_at"));
        inc.setResolvedAt(rs.getTimestamp("resolved_at"));
        return inc;
    }
}
