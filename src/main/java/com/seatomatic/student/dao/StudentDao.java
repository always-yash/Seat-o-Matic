package com.seatomatic.student.dao;

import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.student.model.Student;
import com.seatomatic.student.model.dto.BatchGroupStatsDTO;
import com.seatomatic.student.model.dto.StudentDetailDTO;
import com.seatomatic.student.model.dto.StudentFilter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object managing student database operations with parameterized queries.
 */
public class StudentDao extends BaseDAO {

    private static final String DETAIL_SELECT_BASE =
            "SELECT s.id, s.enrollment_no, s.name, s.section_id, sec.name AS section_name, " +
            "       b.id AS branch_id, b.code AS branch_code, b.name AS branch_name, " +
            "       s.semester_id, sem.number AS semester_number, sem.name AS semester_name, " +
            "       s.status, s.created_at, " +
            "       (SELECT COUNT(*) FROM exam_students es WHERE es.student_id = s.id) AS exam_count, " +
            "       (SELECT COUNT(*) FROM seat_assignments sa WHERE sa.student_id = s.id) AS seat_count " +
            "FROM students s " +
            "JOIN sections sec ON s.section_id = sec.id " +
            "JOIN branches b ON sec.branch_id = b.id " +
            "JOIN semesters sem ON s.semester_id = sem.id ";

    // ==========================================
    // Core CRUD
    // ==========================================

    public Long insert(Student student) throws SQLException {
        if (student == null) throw new IllegalArgumentException("Student must not be null");
        String sql = "INSERT INTO students (enrollment_no, name, section_id, semester_id, status) VALUES (?, ?, ?, ?, ?)";
        String status = (student.getStatus() != null && !student.getStatus().isBlank())
                ? student.getStatus().toUpperCase() : "ACTIVE";
        return executeInsert(sql,
                student.getEnrollmentNo().trim(),
                student.getName().trim(),
                student.getSectionId(),
                student.getSemesterId(),
                status
        );
    }

    public boolean update(Student student) throws SQLException {
        if (student == null || student.getId() == null) {
            throw new IllegalArgumentException("Student and Student ID must not be null");
        }
        String sql = "UPDATE students SET enrollment_no = ?, name = ?, section_id = ?, semester_id = ?, status = ? WHERE id = ?";
        String status = (student.getStatus() != null && !student.getStatus().isBlank())
                ? student.getStatus().toUpperCase() : "ACTIVE";
        int affected = executeUpdate(sql,
                student.getEnrollmentNo().trim(),
                student.getName().trim(),
                student.getSectionId(),
                student.getSemesterId(),
                status,
                student.getId()
        );
        return affected > 0;
    }

    public boolean updateStatus(Long studentId, String status) throws SQLException {
        if (studentId == null || status == null) return false;
        String sql = "UPDATE students SET status = ? WHERE id = ?";
        return executeUpdate(sql, status.trim().toUpperCase(), studentId) > 0;
    }

    public boolean delete(Long studentId) throws SQLException {
        if (studentId == null) return false;
        String sql = "DELETE FROM students WHERE id = ?";
        return executeUpdate(sql, studentId) > 0;
    }

    public Optional<Student> findById(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, enrollment_no, name, section_id, semester_id, status FROM students WHERE id = ?";
        return queryOptional(sql, this::mapBasicStudent, id);
    }

    public Optional<Student> findByEnrollmentNo(String enrollmentNo) throws SQLException {
        if (enrollmentNo == null || enrollmentNo.isBlank()) return Optional.empty();
        String sql = "SELECT id, enrollment_no, name, section_id, semester_id, status FROM students WHERE UPPER(enrollment_no) = UPPER(?)";
        return queryOptional(sql, this::mapBasicStudent, enrollmentNo.trim());
    }

    public boolean existsByEnrollmentNo(String enrollmentNo) throws SQLException {
        if (enrollmentNo == null || enrollmentNo.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM students WHERE UPPER(enrollment_no) = UPPER(?)";
        return queryCount(sql, enrollmentNo.trim()) > 0;
    }

    public boolean existsByEnrollmentNoExcludingId(String enrollmentNo, Long excludeId) throws SQLException {
        if (enrollmentNo == null || enrollmentNo.isBlank()) return false;
        if (excludeId == null) return existsByEnrollmentNo(enrollmentNo);
        String sql = "SELECT COUNT(*) FROM students WHERE UPPER(enrollment_no) = UPPER(?) AND id <> ?";
        return queryCount(sql, enrollmentNo.trim(), excludeId) > 0;
    }

    // ==========================================
    // Detailed Profile Lookups
    // ==========================================

    public Optional<StudentDetailDTO> findDetailById(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        String sql = DETAIL_SELECT_BASE + "WHERE s.id = ?";
        return queryOptional(sql, this::mapDetailStudent, id);
    }

    public Optional<StudentDetailDTO> findDetailByEnrollmentNo(String enrollmentNo) throws SQLException {
        if (enrollmentNo == null || enrollmentNo.isBlank()) return Optional.empty();
        String sql = DETAIL_SELECT_BASE + "WHERE UPPER(s.enrollment_no) = UPPER(?)";
        return queryOptional(sql, this::mapDetailStudent, enrollmentNo.trim());
    }

    // ==========================================
    // Filtered Queries & Pagination
    // ==========================================

    public List<StudentDetailDTO> findWithFilter(StudentFilter filter) throws SQLException {
        StringBuilder sql = new StringBuilder(DETAIL_SELECT_BASE);
        List<Object> params = new ArrayList<>();
        buildFilterClause(filter, sql, params);

        String sortColumn = resolveSortColumn(filter.getSortBy());
        String order = filter.getSortOrder();
        sql.append(" ORDER BY ").append(sortColumn).append(" ").append(order);

        sql.append(" LIMIT ? OFFSET ?");
        params.add(filter.getPageSize());
        params.add(filter.getOffset());

        return queryList(sql.toString(), this::mapDetailStudent, params.toArray());
    }

    public long countWithFilter(StudentFilter filter) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM students s " +
                "JOIN sections sec ON s.section_id = sec.id " +
                "JOIN branches b ON sec.branch_id = b.id " +
                "JOIN semesters sem ON s.semester_id = sem.id "
        );
        List<Object> params = new ArrayList<>();
        buildFilterClause(filter, sql, params);

        return queryCount(sql.toString(), params.toArray());
    }

    public List<StudentDetailDTO> searchInstant(String keyword, int limit) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        int max = limit > 0 ? Math.min(limit, 50) : 10;
        String sql = DETAIL_SELECT_BASE +
                "WHERE (LOWER(s.enrollment_no) LIKE ? OR LOWER(s.name) LIKE ?) " +
                "ORDER BY s.enrollment_no ASC LIMIT ?";
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        return queryList(sql, this::mapDetailStudent, pattern, pattern, max);
    }

    // ==========================================
    // Batch Insert & Metrics
    // ==========================================

    public int[] batchInsert(List<Student> students) throws SQLException {
        if (students == null || students.isEmpty()) {
            return new int[0];
        }
        String sql = "INSERT INTO students (enrollment_no, name, section_id, semester_id, status) VALUES (?, ?, ?, ?, ?)";
        List<Object[]> batchParams = new ArrayList<>(students.size());
        for (Student s : students) {
            String status = (s.getStatus() != null && !s.getStatus().isBlank()) ? s.getStatus().toUpperCase() : "ACTIVE";
            batchParams.add(new Object[]{
                    s.getEnrollmentNo().trim(),
                    s.getName().trim(),
                    s.getSectionId(),
                    s.getSemesterId(),
                    status
            });
        }
        return executeBatchParams(sql, batchParams);
    }

    public List<BatchGroupStatsDTO> countByBranch() throws SQLException {
        String sql = "SELECT b.id AS branch_id, b.code AS branch_code, b.name AS branch_name, " +
                "       COUNT(s.id) AS total_count, " +
                "       SUM(CASE WHEN s.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_count, " +
                "       SUM(CASE WHEN s.status = 'INACTIVE' THEN 1 ELSE 0 END) AS inactive_count " +
                "FROM branches b " +
                "LEFT JOIN sections sec ON sec.branch_id = b.id " +
                "LEFT JOIN students s ON s.section_id = sec.id " +
                "GROUP BY b.id, b.code, b.name " +
                "ORDER BY b.code ASC";

        return queryList(sql, rs -> new BatchGroupStatsDTO(
                rs.getLong("branch_id"),
                rs.getString("branch_code"),
                rs.getString("branch_name"),
                rs.getInt("total_count"),
                rs.getInt("active_count"),
                rs.getInt("inactive_count")
        ));
    }

    public long countTotalStudents() throws SQLException {
        return queryCount("SELECT COUNT(*) FROM students");
    }

    public long countActiveStudents() throws SQLException {
        return queryCount("SELECT COUNT(*) FROM students WHERE status = 'ACTIVE'");
    }

    // ==========================================
    // Helpers
    // ==========================================

    private void buildFilterClause(StudentFilter filter, StringBuilder sql, List<Object> params) {
        if (filter == null) return;

        List<String> conditions = new ArrayList<>();

        if (filter.getKeyword() != null) {
            conditions.add("(LOWER(s.enrollment_no) LIKE ? OR LOWER(s.name) LIKE ?)");
            String pattern = "%" + filter.getKeyword().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (filter.getBranchId() != null) {
            conditions.add("b.id = ?");
            params.add(filter.getBranchId());
        }

        if (filter.getSectionId() != null) {
            conditions.add("s.section_id = ?");
            params.add(filter.getSectionId());
        }

        if (filter.getSemesterId() != null) {
            conditions.add("s.semester_id = ?");
            params.add(filter.getSemesterId());
        }

        if (filter.getStatus() != null) {
            conditions.add("s.status = ?");
            params.add(filter.getStatus());
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
    }

    private String resolveSortColumn(String sortBy) {
        if ("name".equalsIgnoreCase(sortBy)) return "s.name";
        if ("created_at".equalsIgnoreCase(sortBy)) return "s.created_at";
        if ("status".equalsIgnoreCase(sortBy)) return "s.status";
        return "s.enrollment_no";
    }

    private Student mapBasicStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getLong("id"));
        s.setEnrollmentNo(rs.getString("enrollment_no"));
        s.setName(rs.getString("name"));
        s.setSectionId(rs.getLong("section_id"));
        s.setSemesterId(rs.getLong("semester_id"));
        s.setStatus(rs.getString("status"));
        return s;
    }

    private StudentDetailDTO mapDetailStudent(ResultSet rs) throws SQLException {
        StudentDetailDTO dto = new StudentDetailDTO();
        dto.setId(rs.getLong("id"));
        dto.setEnrollmentNo(rs.getString("enrollment_no"));
        dto.setName(rs.getString("name"));
        dto.setSectionId(rs.getLong("section_id"));
        dto.setSectionName(rs.getString("section_name"));
        dto.setBranchId(rs.getLong("branch_id"));
        dto.setBranchCode(rs.getString("branch_code"));
        dto.setBranchName(rs.getString("branch_name"));
        dto.setSemesterId(rs.getLong("semester_id"));
        dto.setSemesterNumber(rs.getInt("semester_number"));
        dto.setSemesterName(rs.getString("semester_name"));
        dto.setStatus(rs.getString("status"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        dto.setExamCount(rs.getInt("exam_count"));
        dto.setSeatAssignmentCount(rs.getInt("seat_count"));
        return dto;
    }
}

