package com.seatomatic.student.service;

import com.google.gson.Gson;
import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.report.audit.AuditPublisherImpl;
import com.seatomatic.student.dao.StudentDao;
import com.seatomatic.student.dao.StudentEligibilityDao;
import com.seatomatic.student.model.Student;
import com.seatomatic.student.model.dto.StudentDetailDTO;
import com.seatomatic.student.model.dto.StudentEligibilityDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Service managing examination qualification, candidate roster alignment,
 * and exam seating inquiries.
 */
public class StudentEligibilityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentEligibilityService.class);
    private static final Gson GSON = new Gson();

    private final StudentEligibilityDao eligibilityDao;
    private final StudentDao studentDao;
    private final AuditPublisher auditPublisher;

    public StudentEligibilityService() {
        this(new StudentEligibilityDao(), new StudentDao(), new AuditPublisherImpl());
    }

    public StudentEligibilityService(StudentEligibilityDao eligibilityDao, StudentDao studentDao, AuditPublisher auditPublisher) {
        this.eligibilityDao = eligibilityDao;
        this.studentDao = studentDao;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Retrieves all scheduled examinations and active seating status for a specific student.
     */
    public List<StudentEligibilityDTO> getEligibleExamsForStudent(Long studentId) throws SQLException {
        if (studentId == null) {
            return List.of();
        }
        return eligibilityDao.findEligibleExamsForStudent(studentId);
    }

    /**
     * Retrieves all students qualified/enrolled for an examination.
     */
    public List<StudentDetailDTO> getEligibleStudentsForExam(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        return eligibilityDao.findEligibleStudentsForExam(examId);
    }

    /**
     * Checks whether an active student is enrolled for an exam.
     */
    public boolean checkEligibility(Long studentId, Long examId) throws SQLException {
        if (studentId == null || examId == null) {
            return false;
        }
        return eligibilityDao.isStudentEligible(studentId, examId);
    }

    /**
     * Enrolls a student into an exam subject syllabus.
     */
    public boolean registerStudentForExam(Long examId, Long studentId, Long subjectId, String actorUsername)
            throws SQLException, ValidationException {
        if (examId == null || studentId == null || subjectId == null) {
            throw new ValidationException("Exam ID, Student ID, and Subject ID are all required.");
        }

        Student student = studentDao.findById(studentId)
                .orElseThrow(() -> new ValidationException("Student not found with ID: " + studentId));

        if (!"ACTIVE".equalsIgnoreCase(student.getStatus())) {
            throw new ValidationException("Cannot register inactive student (id=" + studentId + ") for examinations.");
        }

        boolean registered = eligibilityDao.registerStudentForExam(examId, studentId, subjectId);
        if (registered) {
            LOGGER.info("Enrolled student id={} to exam id={} subject id={}", studentId, examId, subjectId);
            if (auditPublisher != null) {
                auditPublisher.publish("EXAM_STUDENT_REGISTER", actorUsername != null ? actorUsername : "SYSTEM",
                        "EXAM_STUDENT", examId,
                        GSON.toJson(Map.of("studentId", studentId, "subjectId", subjectId)));
            }
        }
        return registered;
    }

    /**
     * Deregisters a student from an examination roster.
     */
    public boolean removeStudentFromExam(Long examId, Long studentId, String actorUsername) throws SQLException {
        if (examId == null || studentId == null) {
            return false;
        }
        boolean removed = eligibilityDao.removeStudentFromExam(examId, studentId);
        if (removed && auditPublisher != null) {
            auditPublisher.publish("EXAM_STUDENT_DEREGISTER", actorUsername != null ? actorUsername : "SYSTEM",
                    "EXAM_STUDENT", examId,
                    GSON.toJson(Map.of("studentId", studentId)));
        }
        return removed;
    }
}
