package com.seatomatic.paper.dao;

import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.paper.model.InvigilatorAssignment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for invigilator room assignments.
 * Enforces room-scoped boundaries by facilitating exact (exam, room, user) checks.
 */
public class InvigilatorAssignmentDao extends BaseDAO {

    private static final String SELECT_BASE =
            "SELECT ia.id, ia.exam_id, ia.room_id, ia.invigilator_id, ia.status, ia.check_in_time, ia.created_at, ia.updated_at, " +
            "       u.username AS invigilator_username, u.username AS invigilator_name, " +
            "       r.room_code, e.title AS exam_title " +
            "FROM invigilator_assignments ia " +
            "JOIN users u ON ia.invigilator_id = u.id " +
            "JOIN rooms r ON ia.room_id = r.id " +
            "JOIN exams e ON ia.exam_id = e.id ";

    public Long insert(InvigilatorAssignment assignment) throws SQLException {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment cannot be null");
        }
        String sql = "INSERT INTO invigilator_assignments (exam_id, room_id, invigilator_id, status) VALUES (?, ?, ?, ?)";
        Long id = executeInsert(sql, assignment.getExamId(), assignment.getRoomId(), assignment.getInvigilatorId(),
                assignment.getStatus() != null ? assignment.getStatus() : "ASSIGNED");
        if (id != null) {
            assignment.setId(id);
        }
        return id;
    }

    public Optional<InvigilatorAssignment> findById(Long id) throws SQLException {
        if (id == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE ia.id = ?";
        return queryOptional(sql, this::mapRow, id);
    }

    public Optional<InvigilatorAssignment> findByExamRoomAndInvigilator(Long examId, Long roomId, Long invigilatorId) throws SQLException {
        if (examId == null || roomId == null || invigilatorId == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE ia.exam_id = ? AND ia.room_id = ? AND ia.invigilator_id = ?";
        return queryOptional(sql, this::mapRow, examId, roomId, invigilatorId);
    }

    public List<InvigilatorAssignment> findByExamId(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE ia.exam_id = ? ORDER BY r.room_code ASC, u.username ASC";
        return queryList(sql, this::mapRow, examId);
    }

    public List<InvigilatorAssignment> findByInvigilatorId(Long invigilatorId) throws SQLException {
        if (invigilatorId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE ia.invigilator_id = ? ORDER BY ia.id DESC";
        return queryList(sql, this::mapRow, invigilatorId);
    }

    public List<InvigilatorAssignment> findByExamAndRoom(Long examId, Long roomId) throws SQLException {
        if (examId == null || roomId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE ia.exam_id = ? AND ia.room_id = ? ORDER BY u.username ASC";
        return queryList(sql, this::mapRow, examId, roomId);
    }

    /**
     * IDOR Security Guard: Returns true if and only if the specified user is assigned to the exam room.
     */
    public boolean isAssigned(Long examId, Long roomId, Long invigilatorId) throws SQLException {
        if (examId == null || roomId == null || invigilatorId == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM invigilator_assignments " +
                     "WHERE exam_id = ? AND room_id = ? AND invigilator_id = ? AND status <> 'COMPLETED'";
        return queryCount(sql, examId, roomId, invigilatorId) > 0;
    }

    public int updateStatus(Long id, String status) throws SQLException {
        if (id == null || status == null || status.isBlank()) {
            throw new IllegalArgumentException("Invalid id or status");
        }
        String sql = "UPDATE invigilator_assignments SET status = ? WHERE id = ?";
        return executeUpdate(sql, status.trim().toUpperCase(), id);
    }

    public int recordCheckIn(Long examId, Long roomId, Long invigilatorId) throws SQLException {
        if (examId == null || roomId == null || invigilatorId == null) {
            return 0;
        }
        String sql = "UPDATE invigilator_assignments SET check_in_time = CURRENT_TIMESTAMP, status = 'ACTIVE' " +
                     "WHERE exam_id = ? AND room_id = ? AND invigilator_id = ? AND check_in_time IS NULL";
        return executeUpdate(sql, examId, roomId, invigilatorId);
    }

    public int delete(Long id) throws SQLException {
        if (id == null) {
            return 0;
        }
        String sql = "DELETE FROM invigilator_assignments WHERE id = ?";
        return executeUpdate(sql, id);
    }

    private InvigilatorAssignment mapRow(ResultSet rs) throws SQLException {
        InvigilatorAssignment a = new InvigilatorAssignment();
        a.setId(rs.getLong("id"));
        a.setExamId(rs.getLong("exam_id"));
        a.setRoomId(rs.getLong("room_id"));
        a.setInvigilatorId(rs.getLong("invigilator_id"));
        a.setStatus(rs.getString("status"));
        try {
            a.setCheckInTime(rs.getTimestamp("check_in_time"));
        } catch (SQLException ignored) {
        }
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setUpdatedAt(rs.getTimestamp("updated_at"));
        a.setInvigilatorUsername(rs.getString("invigilator_username"));
        a.setInvigilatorName(rs.getString("invigilator_name"));
        a.setRoomCode(rs.getString("room_code"));
        a.setExamTitle(rs.getString("exam_title"));
        return a;
    }
}
