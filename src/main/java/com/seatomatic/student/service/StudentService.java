package com.seatomatic.student.service;

import com.google.gson.Gson;
import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.report.audit.AuditPublisherImpl;
import com.seatomatic.student.dao.AcademicLookupDao;
import com.seatomatic.student.dao.StudentDao;
import com.seatomatic.student.model.Branch;
import com.seatomatic.student.model.Section;
import com.seatomatic.student.model.Semester;
import com.seatomatic.student.model.Student;
import com.seatomatic.student.model.dto.BatchGroupStatsDTO;
import com.seatomatic.student.model.dto.Page;
import com.seatomatic.student.model.dto.StudentDetailDTO;
import com.seatomatic.student.model.dto.StudentFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service managing student lifecycle, business validations, profile retrieval,
 * and batch alignments with audit traceability.
 */
public class StudentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentService.class);
    private static final Pattern ENROLLMENT_PATTERN = Pattern.compile("^[A-Za-z0-9/_-]{3,50}$");
    private static final Gson GSON = new Gson();

    private final StudentDao studentDao;
    private final AcademicLookupDao academicLookupDao;
    private final AuditPublisher auditPublisher;

    public StudentService() {
        this(new StudentDao(), new AcademicLookupDao(), new AuditPublisherImpl());
    }

    public StudentService(StudentDao studentDao, AcademicLookupDao academicLookupDao, AuditPublisher auditPublisher) {
        this.studentDao = studentDao;
        this.academicLookupDao = academicLookupDao;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Registers a new student after executing comprehensive uniqueness and referential integrity validations.
     */
    public StudentDetailDTO registerStudent(Student student, String actorUsername) throws SQLException, ValidationException {
        validateStudentInput(student);

        String enrollmentNo = student.getEnrollmentNo().trim();
        if (studentDao.existsByEnrollmentNo(enrollmentNo)) {
            LOGGER.warn("Attempt to register duplicate enrollment number: {}", enrollmentNo);
            throw new ValidationException("A student with enrollment number '" + enrollmentNo + "' is already registered.");
        }

        validateAcademicReferences(student.getSectionId(), student.getSemesterId());

        if (student.getStatus() == null || student.getStatus().isBlank()) {
            student.setStatus("ACTIVE");
        } else {
            student.setStatus(student.getStatus().toUpperCase());
        }

        Long newId = studentDao.insert(student);
        student.setId(newId);
        LOGGER.info("Successfully registered student id={} roll={}", newId, enrollmentNo);

        publishAudit("STUDENT_REGISTER", actorUsername, newId, Map.of(
                "enrollmentNo", enrollmentNo,
                "name", student.getName(),
                "sectionId", student.getSectionId(),
                "semesterId", student.getSemesterId()
        ));

        return studentDao.findDetailById(newId)
                .orElseThrow(() -> new SQLException("Failed to retrieve created student record with id: " + newId));
    }

    /**
     * Updates an existing student record with duplicate-checking across other records.
     */
    public StudentDetailDTO updateStudent(Student student, String actorUsername) throws SQLException, ValidationException {
        if (student == null || student.getId() == null) {
            throw new ValidationException("Student record and ID must not be null.");
        }

        validateStudentInput(student);

        Long studentId = student.getId();
        Optional<Student> existingOpt = studentDao.findById(studentId);
        if (existingOpt.isEmpty()) {
            LOGGER.warn("Student record not found for update, id={}", studentId);
            throw new ValidationException("Student record not found for ID: " + studentId);
        }

        String enrollmentNo = student.getEnrollmentNo().trim();
        if (studentDao.existsByEnrollmentNoExcludingId(enrollmentNo, studentId)) {
            LOGGER.warn("Duplicate enrollment conflict on update: id={} roll={}", studentId, enrollmentNo);
            throw new ValidationException("Another student with enrollment number '" + enrollmentNo + "' already exists.");
        }

        validateAcademicReferences(student.getSectionId(), student.getSemesterId());

        studentDao.update(student);
        LOGGER.info("Updated student id={} roll={}", studentId, enrollmentNo);

        publishAudit("STUDENT_UPDATE", actorUsername, studentId, Map.of(
                "enrollmentNo", enrollmentNo,
                "name", student.getName(),
                "status", student.getStatus()
        ));

        return studentDao.findDetailById(studentId)
                .orElseThrow(() -> new SQLException("Failed to retrieve updated student record with id: " + studentId));
    }

    /**
     * Toggles a student's active status.
     */
    public boolean toggleStatus(Long studentId, String actorUsername) throws SQLException, ValidationException {
        if (studentId == null) {
            throw new ValidationException("Student ID is required.");
        }
        Student student = studentDao.findById(studentId)
                .orElseThrow(() -> new ValidationException("Student not found with ID: " + studentId));

        String newStatus = "ACTIVE".equalsIgnoreCase(student.getStatus()) ? "INACTIVE" : "ACTIVE";
        boolean success = studentDao.updateStatus(studentId, newStatus);
        if (success) {
            LOGGER.info("Toggled student status id={} newStatus={}", studentId, newStatus);
            publishAudit("STUDENT_STATUS_TOGGLE", actorUsername, studentId, Map.of(
                    "previousStatus", student.getStatus(),
                    "newStatus", newStatus
            ));
        }
        return success;
    }

    /**
     * Deletes a student if no critical foreign key dependencies block the deletion.
     */
    public boolean deleteStudent(Long studentId, String actorUsername) throws SQLException, ValidationException {
        if (studentId == null) {
            throw new ValidationException("Student ID is required.");
        }
        Student student = studentDao.findById(studentId)
                .orElseThrow(() -> new ValidationException("Student not found with ID: " + studentId));

        boolean deleted = studentDao.delete(studentId);
        if (deleted) {
            LOGGER.info("Deleted student id={} roll={}", studentId, student.getEnrollmentNo());
            publishAudit("STUDENT_DELETE", actorUsername, studentId, Map.of(
                    "enrollmentNo", student.getEnrollmentNo(),
                    "name", student.getName()
            ));
        }
        return deleted;
    }

    public Optional<Student> getStudentById(Long id) throws SQLException {
        return studentDao.findById(id);
    }

    public Optional<StudentDetailDTO> getStudentDetail(Long id) throws SQLException {
        return studentDao.findDetailById(id);
    }

    public Optional<StudentDetailDTO> getStudentDetailByEnrollment(String enrollmentNo) throws SQLException {
        return studentDao.findDetailByEnrollmentNo(enrollmentNo);
    }

    /**
     * Fetches a paginated result set of students matching the filter criteria.
     */
    public Page<StudentDetailDTO> listStudents(StudentFilter filter) throws SQLException {
        if (filter == null) {
            filter = new StudentFilter();
        }
        long totalCount = studentDao.countWithFilter(filter);
        List<StudentDetailDTO> items = studentDao.findWithFilter(filter);
        return new Page<>(items, totalCount, filter.getPage(), filter.getPageSize());
    }

    /**
     * Fast live search query for auto-complete and instant filtering.
     */
    public List<StudentDetailDTO> instantSearch(String keyword, int limit) throws SQLException {
        return studentDao.searchInstant(keyword, limit);
    }

    public List<BatchGroupStatsDTO> getBatchStatistics() throws SQLException {
        return studentDao.countByBranch();
    }

    public long getTotalStudentCount() throws SQLException {
        return studentDao.countTotalStudents();
    }

    public long getActiveStudentCount() throws SQLException {
        return studentDao.countActiveStudents();
    }

    // ==========================================
    // Academic Hierarchy Lookups
    // ==========================================

    public List<Branch> getAllBranches() throws SQLException {
        return academicLookupDao.findAllBranches();
    }

    public List<Section> getSectionsByBranch(Long branchId) throws SQLException {
        return academicLookupDao.findSectionsByBranchId(branchId);
    }

    public List<Semester> getAllSemesters() throws SQLException {
        return academicLookupDao.findAllSemesters();
    }

    // ==========================================
    // Validation Helpers
    // ==========================================

    private void validateStudentInput(Student student) throws ValidationException {
        if (student == null) {
            throw new ValidationException("Student payload cannot be empty.");
        }

        String enrollmentNo = student.getEnrollmentNo();
        if (enrollmentNo == null || enrollmentNo.isBlank()) {
            throw new ValidationException("Roll number / Enrollment number is required.");
        }
        enrollmentNo = enrollmentNo.trim();
        if (!ENROLLMENT_PATTERN.matcher(enrollmentNo).matches()) {
            throw new ValidationException("Roll number format is invalid. Use 3-50 alphanumeric characters or dashes.");
        }

        String name = student.getName();
        if (name == null || name.isBlank()) {
            throw new ValidationException("Student full name is required.");
        }
        if (name.trim().length() < 2 || name.trim().length() > 120) {
            throw new ValidationException("Student name must be between 2 and 120 characters.");
        }

        if (student.getSectionId() == null || student.getSectionId() <= 0) {
            throw new ValidationException("Academic section selection is required.");
        }

        if (student.getSemesterId() == null || student.getSemesterId() <= 0) {
            throw new ValidationException("Academic semester selection is required.");
        }
    }

    private void validateAcademicReferences(Long sectionId, Long semesterId) throws SQLException, ValidationException {
        Optional<Section> sectionOpt = academicLookupDao.findSectionById(sectionId);
        if (sectionOpt.isEmpty()) {
            throw new ValidationException("Invalid section ID: section does not exist in the database.");
        }

        Optional<Semester> semesterOpt = academicLookupDao.findSemesterById(semesterId);
        if (semesterOpt.isEmpty()) {
            throw new ValidationException("Invalid semester ID: semester does not exist in the database.");
        }
    }

    private void publishAudit(String action, String actor, Long targetId, Map<String, Object> details) {
        if (auditPublisher != null) {
            try {
                String actorName = (actor != null && !actor.isBlank()) ? actor : "SYSTEM";
                String jsonDetails = GSON.toJson(details != null ? details : new HashMap<>());
                auditPublisher.publish(action, actorName, "STUDENT", targetId, jsonDetails);
            } catch (Exception ex) {
                LOGGER.warn("Failed to publish student audit event: {}", ex.getMessage());
            }
        }
    }
}

