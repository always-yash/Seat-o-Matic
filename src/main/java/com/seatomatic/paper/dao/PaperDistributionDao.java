package com.seatomatic.paper.dao;

import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.paper.model.DistributionStatus;
import com.seatomatic.paper.model.PaperDistribution;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for paper distribution tracking and state lifecycle.
 * Protects against SQL injection by using BaseDAO parameterized calls.
 */
public class PaperDistributionDao extends BaseDAO {

    private static final String SELECT_BASE =
            "SELECT pd.id, pd.plan_id, pd.room_id, pd.paper_id, pd.required, pd.buffer, " +
            "       pd.distributed, pd.collected, pd.status, pd.created_at, pd.updated_at, " +
            "       COALESCE(pd.version, 0) AS version, " +
            "       r.room_code, qp.exam_id, qp.subject_id, qp.set_code, s.code AS subject_code, s.name AS subject_name " +
            "FROM paper_distribution pd " +
            "JOIN rooms r ON pd.room_id = r.id " +
            "JOIN question_papers qp ON pd.paper_id = qp.id " +
            "JOIN subjects s ON qp.subject_id = s.id ";

    public Long insert(PaperDistribution dist) throws SQLException {
        if (dist == null) {
            throw new IllegalArgumentException("PaperDistribution cannot be null");
        }
        String sql = "INSERT INTO paper_distribution (plan_id, room_id, paper_id, required, buffer, distributed, collected, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Long id = executeInsert(sql, dist.getPlanId(), dist.getRoomId(), dist.getPaperId(),
                dist.getRequired(), dist.getBuffer(), dist.getDistributed(), dist.getCollected(),
                dist.getStatus().name());
        if (id != null) {
            dist.setId(id);
        }
        return id;
    }

    /**
     * Safely inserts or updates paper requirement without overwriting legitimate progress.
     */
    public int upsertRequirement(Long planId, Long roomId, Long paperId, int required, int buffer) throws SQLException {
        if (planId == null || roomId == null || paperId == null) {
            throw new IllegalArgumentException("Identifiers must not be null");
        }
        String sql = "INSERT INTO paper_distribution (plan_id, room_id, paper_id, required, buffer, status) " +
                     "VALUES (?, ?, ?, ?, ?, 'PENDING') " +
                     "ON DUPLICATE KEY UPDATE required = VALUES(required), buffer = VALUES(buffer)";
        return executeUpdate(sql, planId, roomId, paperId, required, buffer);
    }

    public Optional<PaperDistribution> findById(Long id) throws SQLException {
        if (id == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE pd.id = ?";
        return queryOptional(sql, this::mapRow, id);
    }

    public Optional<PaperDistribution> findByPlanRoomAndPaper(Long planId, Long roomId, Long paperId) throws SQLException {
        if (planId == null || roomId == null || paperId == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE pd.plan_id = ? AND pd.room_id = ? AND pd.paper_id = ?";
        return queryOptional(sql, this::mapRow, planId, roomId, paperId);
    }

    public List<PaperDistribution> findByPlanId(Long planId) throws SQLException {
        if (planId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE pd.plan_id = ? ORDER BY r.room_code ASC, s.code ASC";
        return queryList(sql, this::mapRow, planId);
    }

    public List<PaperDistribution> findByPlanAndRoom(Long planId, Long roomId) throws SQLException {
        if (planId == null || roomId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE pd.plan_id = ? AND pd.room_id = ? ORDER BY s.code ASC";
        return queryList(sql, this::mapRow, planId, roomId);
    }

    public List<PaperDistribution> findByRoomId(Long roomId) throws SQLException {
        if (roomId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE pd.room_id = ? ORDER BY pd.id DESC";
        return queryList(sql, this::mapRow, roomId);
    }

    public List<PaperDistribution> findByExamId(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE qp.exam_id = ? ORDER BY r.room_code ASC, s.code ASC";
        return queryList(sql, this::mapRow, examId);
    }

    public int updateStatus(Long id, DistributionStatus newStatus) throws SQLException {
        if (id == null || newStatus == null) {
            throw new IllegalArgumentException("Id and status must not be null");
        }
        String sql = "UPDATE paper_distribution SET status = ? WHERE id = ?";
        return executeUpdate(sql, newStatus.name(), id);
    }

    public int updateDistributed(Long id, int distributed, DistributionStatus newStatus) throws SQLException {
        if (id == null || distributed < 0 || newStatus == null) {
            throw new IllegalArgumentException("Invalid distributed count or status");
        }
        String sql = "UPDATE paper_distribution SET distributed = ?, status = ? WHERE id = ?";
        return executeUpdate(sql, distributed, newStatus.name(), id);
    }

    public int updateCollected(Long id, int collected, DistributionStatus newStatus) throws SQLException {
        if (id == null || collected < 0 || newStatus == null) {
            throw new IllegalArgumentException("Invalid collected count or status");
        }
        String sql = "UPDATE paper_distribution SET collected = ?, status = ? WHERE id = ?";
        return executeUpdate(sql, collected, newStatus.name(), id);
    }

    public long countByPlanAndStatus(Long planId, DistributionStatus status) throws SQLException {
        if (planId == null || status == null) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM paper_distribution WHERE plan_id = ? AND status = ?";
        return queryCount(sql, planId, status.name());
    }

    public boolean isDistributionCompleteForRoom(Long roomId) throws SQLException {
        if (roomId == null) {
            return false;
        }
        // Returns true if there is at least one distribution record and NONE are in PENDING or PREPARED
        String totalSql = "SELECT COUNT(*) FROM paper_distribution WHERE room_id = ?";
        long total = queryCount(totalSql, roomId);
        if (total == 0) {
            return false;
        }
        String incompleteSql = "SELECT COUNT(*) FROM paper_distribution WHERE room_id = ? AND status IN ('PENDING', 'PREPARED')";
        long incomplete = queryCount(incompleteSql, roomId);
        return incomplete == 0;
    }

    public int getRequiredCopies(Long roomId, Long subjectId) throws SQLException {
        if (roomId == null || subjectId == null) {
            return 0;
        }
        String sql = "SELECT COALESCE(SUM(pd.required), 0) FROM paper_distribution pd " +
                     "JOIN question_papers qp ON pd.paper_id = qp.id " +
                     "WHERE pd.room_id = ? AND qp.subject_id = ?";
        Long sum = queryOne(sql, rs -> rs.getLong(1), roomId, subjectId);
        return sum != null ? sum.intValue() : 0;
    }

    public int getAggregateRequiredForPaper(Long examId, Long paperId) throws SQLException {
        if (examId == null || paperId == null) {
            return 0;
        }
        String sql = "SELECT COALESCE(SUM(pd.required), 0) FROM paper_distribution pd " +
                     "JOIN question_papers qp ON pd.paper_id = qp.id " +
                     "WHERE qp.exam_id = ? AND pd.paper_id = ?";
        Long sum = queryOne(sql, rs -> rs.getLong(1), examId, paperId);
        return sum != null ? sum.intValue() : 0;
    }

    public int updateStatusWithVersion(Long id, DistributionStatus newStatus, int expectedVersion) throws SQLException {
        if (id == null || newStatus == null) {
            throw new IllegalArgumentException("Id and status must not be null");
        }
        String sql = "UPDATE paper_distribution SET status = ?, version = version + 1 WHERE id = ? AND version = ?";
        return executeUpdate(sql, newStatus.name(), id, expectedVersion);
    }

    public int updateDistributedWithVersion(Long id, int distributed, DistributionStatus newStatus, int expectedVersion) throws SQLException {
        if (id == null || distributed < 0 || newStatus == null) {
            throw new IllegalArgumentException("Invalid distributed count or status");
        }
        String sql = "UPDATE paper_distribution SET distributed = ?, status = ?, version = version + 1 WHERE id = ? AND version = ?";
        return executeUpdate(sql, distributed, newStatus.name(), id, expectedVersion);
    }

    public int updateCollectedWithVersion(Long id, int collected, DistributionStatus newStatus, int expectedVersion) throws SQLException {
        if (id == null || collected < 0 || newStatus == null) {
            throw new IllegalArgumentException("Invalid collected count or status");
        }
        String sql = "UPDATE paper_distribution SET collected = ?, status = ?, version = version + 1 WHERE id = ? AND version = ?";
        return executeUpdate(sql, collected, newStatus.name(), id, expectedVersion);
    }

    private PaperDistribution mapRow(ResultSet rs) throws SQLException {
        PaperDistribution dist = new PaperDistribution();
        dist.setId(rs.getLong("id"));
        dist.setPlanId(rs.getLong("plan_id"));
        dist.setRoomId(rs.getLong("room_id"));
        dist.setPaperId(rs.getLong("paper_id"));
        dist.setRequired(rs.getInt("required"));
        dist.setBuffer(rs.getInt("buffer"));
        dist.setDistributed(rs.getInt("distributed"));
        dist.setCollected(rs.getInt("collected"));
        dist.setStatus(DistributionStatus.fromString(rs.getString("status")));
        dist.setCreatedAt(rs.getTimestamp("created_at"));
        dist.setUpdatedAt(rs.getTimestamp("updated_at"));
        try {
            dist.setVersion(rs.getInt("version"));
        } catch (SQLException ignored) {
            dist.setVersion(0);
        }
        dist.setRoomCode(rs.getString("room_code"));
        dist.setExamId(rs.getLong("exam_id"));
        dist.setSubjectId(rs.getLong("subject_id"));
        dist.setSubjectCode(rs.getString("subject_code"));
        dist.setSubjectName(rs.getString("subject_name"));
        dist.setSetCode(rs.getString("set_code"));
        return dist;
    }
}
