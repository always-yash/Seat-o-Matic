package com.seatomatic.paper.dao;

import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.paper.model.QuestionPaper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for question paper sets management.
 * Guarantees parameterized SQL statements through BaseDAO.
 */
public class QuestionPaperDao extends BaseDAO {

    private static final String SELECT_BASE =
            "SELECT qp.id, qp.exam_id, qp.subject_id, qp.set_code, qp.total_copies, qp.created_at, " +
            "       e.title AS exam_title, s.code AS subject_code, s.name AS subject_name " +
            "FROM question_papers qp " +
            "JOIN exams e ON qp.exam_id = e.id " +
            "JOIN subjects s ON qp.subject_id = s.id ";

    public Long insert(QuestionPaper paper) throws SQLException {
        if (paper == null) {
            throw new IllegalArgumentException("QuestionPaper must not be null");
        }
        String sql = "INSERT INTO question_papers (exam_id, subject_id, set_code, total_copies) VALUES (?, ?, ?, ?)";
        Long id = executeInsert(sql, paper.getExamId(), paper.getSubjectId(), paper.getSetCode().trim().toUpperCase(), paper.getTotalCopies());
        if (id != null) {
            paper.setId(id);
        }
        return id;
    }

    public Optional<QuestionPaper> findById(Long id) throws SQLException {
        if (id == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE qp.id = ?";
        return queryOptional(sql, this::mapRow, id);
    }

    public Optional<QuestionPaper> findByExamSubjectAndSet(Long examId, Long subjectId, String setCode) throws SQLException {
        if (examId == null || subjectId == null || setCode == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE qp.exam_id = ? AND qp.subject_id = ? AND UPPER(qp.set_code) = UPPER(?)";
        return queryOptional(sql, this::mapRow, examId, subjectId, setCode.trim());
    }

    public List<QuestionPaper> findByExamId(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE qp.exam_id = ? ORDER BY s.code ASC, qp.set_code ASC";
        return queryList(sql, this::mapRow, examId);
    }

    public List<QuestionPaper> findBySubjectId(Long subjectId) throws SQLException {
        if (subjectId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE qp.subject_id = ? ORDER BY qp.created_at DESC";
        return queryList(sql, this::mapRow, subjectId);
    }

    public List<QuestionPaper> findAll() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY qp.id DESC";
        return queryList(sql, this::mapRow);
    }

    public int updateStock(Long id, int totalCopies) throws SQLException {
        if (id == null || totalCopies < 0) {
            throw new IllegalArgumentException("Invalid id or stock count");
        }
        String sql = "UPDATE question_papers SET total_copies = ? WHERE id = ?";
        return executeUpdate(sql, totalCopies, id);
    }

    public boolean existsDuplicateSet(Long examId, Long subjectId, String setCode, Long excludeId) throws SQLException {
        if (examId == null || subjectId == null || setCode == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM question_papers WHERE exam_id = ? AND subject_id = ? AND UPPER(set_code) = UPPER(?)" +
                     (excludeId != null ? " AND id <> ?" : "");
        long count;
        if (excludeId != null) {
            count = queryCount(sql, examId, subjectId, setCode.trim(), excludeId);
        } else {
            count = queryCount(sql, examId, subjectId, setCode.trim());
        }
        return count > 0;
    }

    public boolean hasActiveDistributions(Long paperId) throws SQLException {
        if (paperId == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM paper_distribution WHERE paper_id = ?";
        return queryCount(sql, paperId) > 0;
    }

    public int delete(Long id) throws SQLException {
        if (id == null) {
            return 0;
        }
        String sql = "DELETE FROM question_papers WHERE id = ?";
        return executeUpdate(sql, id);
    }

    private QuestionPaper mapRow(ResultSet rs) throws SQLException {
        QuestionPaper qp = new QuestionPaper();
        qp.setId(rs.getLong("id"));
        qp.setExamId(rs.getLong("exam_id"));
        qp.setSubjectId(rs.getLong("subject_id"));
        qp.setSetCode(rs.getString("set_code"));
        qp.setTotalCopies(rs.getInt("total_copies"));
        qp.setCreatedAt(rs.getTimestamp("created_at"));
        qp.setExamTitle(rs.getString("exam_title"));
        qp.setSubjectCode(rs.getString("subject_code"));
        qp.setSubjectName(rs.getString("subject_name"));
        return qp;
    }
}
