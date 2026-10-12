package com.seatomatic.student.dao;

import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.student.model.dto.StudentDetailDTO;
import com.seatomatic.student.model.dto.StudentEligibilityDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for exam eligibility lookups, seating allocation cross-references,
 * and exam candidate registrations.
 */
public class StudentEligibilityDao extends BaseDAO {

    public List<StudentEligibilityDTO> findEligibleExamsForStudent(Long studentId) throws SQLException {
        if (studentId == null) {
            return List.of();
        }
        String sql =
                "SELECT es.student_id, s.name AS student_name, s.enrollment_no, " +
                "       e.id AS exam_id, e.title AS exam_title, e.exam_date, e.start_time, e.end_time, e.status AS exam_status, " +
                "       sub.id AS subject_id, sub.code AS subject_code, sub.name AS subject_name, " +
                "       seat_info.room_code, seat_info.row_no AS seat_row, seat_info.col_no AS seat_col " +
                "FROM exam_students es " +
                "JOIN students s ON es.student_id = s.id " +
                "JOIN exams e ON es.exam_id = e.id " +
                "JOIN subjects sub ON es.subject_id = sub.id " +
                "LEFT JOIN ( " +
                "    SELECT sp.exam_id, a.student_id, r.room_code, st.row_no, st.col_no " +
                "    FROM seating_plans sp " +
                "    JOIN seat_assignments a ON a.plan_id = sp.id " +
                "    JOIN seats st ON a.seat_id = st.id " +
                "    JOIN rooms r ON st.room_id = r.id " +
                "    WHERE sp.status IN ('APPROVED', 'GENERATED', 'REVIEWED', 'LOCKED') " +
                ") seat_info ON seat_info.exam_id = e.id AND seat_info.student_id = s.id " +
                "WHERE es.student_id = ? " +
                "ORDER BY e.exam_date DESC, e.start_time DESC";

        return queryList(sql, rs -> {
            StudentEligibilityDTO dto = new StudentEligibilityDTO();
            dto.setStudentId(rs.getLong("student_id"));
            dto.setStudentName(rs.getString("student_name"));
            dto.setEnrollmentNo(rs.getString("enrollment_no"));
            dto.setExamId(rs.getLong("exam_id"));
            dto.setExamTitle(rs.getString("exam_title"));
            dto.setExamDate(rs.getDate("exam_date"));
            dto.setStartTime(rs.getTime("start_time"));
            dto.setEndTime(rs.getTime("end_time"));
            dto.setExamStatus(rs.getString("exam_status"));
            dto.setSubjectId(rs.getLong("subject_id"));
            dto.setSubjectCode(rs.getString("subject_code"));
            dto.setSubjectName(rs.getString("subject_name"));
            dto.setEligible(true);

            String room = rs.getString("room_code");
            if (room != null) {
                dto.setSeated(true);
                dto.setRoomCode(room);
                dto.setSeatRow(rs.getInt("seat_row"));
                dto.setSeatCol(rs.getInt("seat_col"));
            } else {
                dto.setSeated(false);
            }
            return dto;
        }, studentId);
    }

    public List<StudentDetailDTO> findEligibleStudentsForExam(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        String sql =
                "SELECT s.id, s.enrollment_no, s.name, s.section_id, sec.name AS section_name, " +
                "       b.id AS branch_id, b.code AS branch_code, b.name AS branch_name, " +
                "       s.semester_id, sem.number AS semester_number, sem.name AS semester_name, " +
                "       s.status, s.created_at, " +
                "       1 AS exam_count, " +
                "       (SELECT COUNT(*) FROM seat_assignments sa WHERE sa.student_id = s.id) AS seat_count " +
                "FROM exam_students es " +
                "JOIN students s ON es.student_id = s.id " +
                "JOIN sections sec ON s.section_id = sec.id " +
                "JOIN branches b ON sec.branch_id = b.id " +
                "JOIN semesters sem ON s.semester_id = sem.id " +
                "WHERE es.exam_id = ? " +
                "ORDER BY s.enrollment_no ASC";

        return queryList(sql, rs -> {
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
        }, examId);
    }

    public boolean isStudentEligible(Long studentId, Long examId) throws SQLException {
        if (studentId == null || examId == null) {
            return false;
        }
        String sql =
                "SELECT COUNT(*) FROM exam_students es " +
                "JOIN students s ON es.student_id = s.id " +
                "WHERE es.student_id = ? AND es.exam_id = ? AND s.status = 'ACTIVE'";
        return queryCount(sql, studentId, examId) > 0;
    }

    public int countEligibleStudentsForExam(Long examId) throws SQLException {
        if (examId == null) return 0;
        String sql = "SELECT COUNT(DISTINCT student_id) FROM exam_students WHERE exam_id = ?";
        return (int) queryCount(sql, examId);
    }

    public int countStudentsByBranch(String branchCode) throws SQLException {
        if (branchCode == null || branchCode.isBlank()) return 0;
        String sql =
                "SELECT COUNT(s.id) FROM students s " +
                "JOIN sections sec ON s.section_id = sec.id " +
                "JOIN branches b ON sec.branch_id = b.id " +
                "WHERE UPPER(b.code) = UPPER(?)";
        return (int) queryCount(sql, branchCode.trim());
    }

    public boolean registerStudentForExam(Long examId, Long studentId, Long subjectId) throws SQLException {
        if (examId == null || studentId == null || subjectId == null) {
            return false;
        }
        String sql = "INSERT INTO exam_students (exam_id, student_id, subject_id) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE subject_id = VALUES(subject_id)";
        int affected = executeUpdate(sql, examId, studentId, subjectId);
        return affected > 0;
    }

    public boolean removeStudentFromExam(Long examId, Long studentId) throws SQLException {
        if (examId == null || studentId == null) {
            return false;
        }
        String sql = "DELETE FROM exam_students WHERE exam_id = ? AND student_id = ?";
        return executeUpdate(sql, examId, studentId) > 0;
    }
}

