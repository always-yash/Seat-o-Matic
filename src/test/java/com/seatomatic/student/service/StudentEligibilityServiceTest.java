package com.seatomatic.student.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.student.dao.StudentDao;
import com.seatomatic.student.dao.StudentEligibilityDao;
import com.seatomatic.student.model.Student;
import com.seatomatic.student.model.dto.StudentEligibilityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for examination qualification and seating lookup services.
 */
class StudentEligibilityServiceTest {

    private StudentEligibilityDao mockEligibilityDao;
    private StudentDao mockStudentDao;
    private AuditPublisher mockAuditPublisher;
    private StudentEligibilityService eligibilityService;
    private StudentQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        mockEligibilityDao = mock(StudentEligibilityDao.class);
        mockStudentDao = mock(StudentDao.class);
        mockAuditPublisher = mock(AuditPublisher.class);

        eligibilityService = new StudentEligibilityService(mockEligibilityDao, mockStudentDao, mockAuditPublisher);
        queryService = new StudentQueryServiceImpl(mockEligibilityDao, mockStudentDao);
    }

    @Test
    @DisplayName("Fetch eligible examination schedules with seating records for a student")
    void testGetEligibleExamsForStudent() throws Exception {
        StudentEligibilityDTO exam = new StudentEligibilityDTO();
        exam.setExamId(100L);
        exam.setExamTitle("Midterm CS");
        exam.setSeated(true);
        exam.setRoomCode("Room 101");
        exam.setSeatRow(2);
        exam.setSeatCol(3);

        when(mockEligibilityDao.findEligibleExamsForStudent(5L)).thenReturn(List.of(exam));

        List<StudentEligibilityDTO> results = eligibilityService.getEligibleExamsForStudent(5L);

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSeated());
        assertEquals("Room 101 (Row 2, Col 3)", results.get(0).getSeatLocation());
    }

    @Test
    @DisplayName("Register eligible active student for exam syllabus")
    void testRegisterActiveStudentForExam() throws Exception {
        Student activeStudent = new Student();
        activeStudent.setId(5L);
        activeStudent.setStatus("ACTIVE");

        when(mockStudentDao.findById(5L)).thenReturn(Optional.of(activeStudent));
        when(mockEligibilityDao.registerStudentForExam(100L, 5L, 20L)).thenReturn(true);

        boolean enrolled = eligibilityService.registerStudentForExam(100L, 5L, 20L, "admin");

        assertTrue(enrolled);
        verify(mockEligibilityDao).registerStudentForExam(100L, 5L, 20L);
        verify(mockAuditPublisher).publish(eq("EXAM_STUDENT_REGISTER"), eq("admin"), eq("EXAM_STUDENT"), eq(100L), anyString());
    }

    @Test
    @DisplayName("Reject exam enrollment when student status is INACTIVE")
    void testRegisterInactiveStudentThrows() throws Exception {
        Student inactiveStudent = new Student();
        inactiveStudent.setId(5L);
        inactiveStudent.setStatus("INACTIVE");

        when(mockStudentDao.findById(5L)).thenReturn(Optional.of(inactiveStudent));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                eligibilityService.registerStudentForExam(100L, 5L, 20L, "admin")
        );
        assertTrue(ex.getMessage().contains("Cannot register inactive student"));
        verify(mockEligibilityDao, never()).registerStudentForExam(any(), any(), any());
    }

    @Test
    @DisplayName("StudentQueryService returns count and entity lookups properly")
    void testStudentQueryServiceContract() throws Exception {
        when(mockEligibilityDao.countStudentsByBranch("CSE")).thenReturn(42);

        Student student = new Student();
        student.setId(10L);
        when(mockStudentDao.findById(10L)).thenReturn(Optional.of(student));

        int count = queryService.countByGroup("CSE");
        assertEquals(42, count);

        Long studentId = queryService.getById(10L);
        assertEquals(10L, studentId);
    }
}
