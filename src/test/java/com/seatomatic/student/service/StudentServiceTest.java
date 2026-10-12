package com.seatomatic.student.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.student.dao.AcademicLookupDao;
import com.seatomatic.student.dao.StudentDao;
import com.seatomatic.student.model.Branch;
import com.seatomatic.student.model.Section;
import com.seatomatic.student.model.Semester;
import com.seatomatic.student.model.Student;
import com.seatomatic.student.model.dto.Page;
import com.seatomatic.student.model.dto.StudentDetailDTO;
import com.seatomatic.student.model.dto.StudentFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for StudentService business validation, registration,
 * status transitions, and audit traceability.
 */
class StudentServiceTest {

    private StudentDao mockStudentDao;
    private AcademicLookupDao mockLookupDao;
    private AuditPublisher mockAuditPublisher;
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        mockStudentDao = mock(StudentDao.class);
        mockLookupDao = mock(AcademicLookupDao.class);
        mockAuditPublisher = mock(AuditPublisher.class);

        studentService = new StudentService(mockStudentDao, mockLookupDao, mockAuditPublisher);
    }

    @Test
    @DisplayName("Successfully register student and publish audit event")
    void testRegisterStudentSuccess() throws Exception {
        Student input = new Student();
        input.setEnrollmentNo("2026CS101");
        input.setName("Aarav Sharma");
        input.setSectionId(1L);
        input.setSemesterId(4L);
        input.setStatus("ACTIVE");

        Section mockSection = new Section();
        mockSection.setId(1L);
        mockSection.setName("Section A");

        Semester mockSemester = new Semester(4L, 4, "Semester 4");

        StudentDetailDTO mockDetail = new StudentDetailDTO();
        mockDetail.setId(50L);
        mockDetail.setEnrollmentNo("2026CS101");
        mockDetail.setName("Aarav Sharma");

        when(mockStudentDao.existsByEnrollmentNo("2026CS101")).thenReturn(false);
        when(mockLookupDao.findSectionById(1L)).thenReturn(Optional.of(mockSection));
        when(mockLookupDao.findSemesterById(4L)).thenReturn(Optional.of(mockSemester));
        when(mockStudentDao.insert(any(Student.class))).thenReturn(50L);
        when(mockStudentDao.findDetailById(50L)).thenReturn(Optional.of(mockDetail));

        StudentDetailDTO result = studentService.registerStudent(input, "admin_user");

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals("2026CS101", result.getEnrollmentNo());
        verify(mockStudentDao).insert(any(Student.class));
        verify(mockAuditPublisher).publish(eq("STUDENT_REGISTER"), eq("admin_user"), eq("STUDENT"), eq(50L), anyString());
    }

    @Test
    @DisplayName("Reject student registration when enrollment number is duplicate")
    void testRegisterDuplicateEnrollmentThrows() throws Exception {
        Student input = new Student();
        input.setEnrollmentNo("2026CS101");
        input.setName("Test Student");
        input.setSectionId(1L);
        input.setSemesterId(1L);

        when(mockStudentDao.existsByEnrollmentNo("2026CS101")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                studentService.registerStudent(input, "admin")
        );
        assertTrue(ex.getMessage().contains("already registered"));
        verify(mockStudentDao, never()).insert(any());
    }

    @Test
    @DisplayName("Reject student registration when section does not exist")
    void testRegisterInvalidSectionThrows() throws Exception {
        Student input = new Student();
        input.setEnrollmentNo("2026CS999");
        input.setName("Test Student");
        input.setSectionId(999L);
        input.setSemesterId(1L);

        when(mockStudentDao.existsByEnrollmentNo("2026CS999")).thenReturn(false);
        when(mockLookupDao.findSectionById(999L)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(ValidationException.class, () ->
                studentService.registerStudent(input, "admin")
        );
        assertTrue(ex.getMessage().contains("Invalid section ID"));
    }

    @Test
    @DisplayName("Reject student registration when semester does not exist")
    void testRegisterInvalidSemesterThrows() throws Exception {
        Student input = new Student();
        input.setEnrollmentNo("2026CS999");
        input.setName("Test Student");
        input.setSectionId(1L);
        input.setSemesterId(99L);

        when(mockStudentDao.existsByEnrollmentNo("2026CS999")).thenReturn(false);
        when(mockLookupDao.findSectionById(1L)).thenReturn(Optional.of(new Section()));
        when(mockLookupDao.findSemesterById(99L)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(ValidationException.class, () ->
                studentService.registerStudent(input, "admin")
        );
        assertTrue(ex.getMessage().contains("Invalid semester ID"));
    }

    @Test
    @DisplayName("Reject student registration when enrollment number format is invalid")
    void testRegisterInvalidEnrollmentFormatThrows() {
        Student input = new Student();
        input.setEnrollmentNo("!@#$ invalid");
        input.setName("Test Student");
        input.setSectionId(1L);
        input.setSemesterId(1L);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                studentService.registerStudent(input, "admin")
        );
        assertTrue(ex.getMessage().contains("Roll number format is invalid"));
    }

    @Test
    @DisplayName("Successfully update student details and publish audit")
    void testUpdateStudentSuccess() throws Exception {
        Student existing = new Student();
        existing.setId(25L);
        existing.setEnrollmentNo("2026CS101");
        existing.setName("Aarav");
        existing.setSectionId(1L);
        existing.setSemesterId(2L);
        existing.setStatus("ACTIVE");

        Student updateInput = new Student();
        updateInput.setId(25L);
        updateInput.setEnrollmentNo("2026CS101");
        updateInput.setName("Aarav Sharma Updated");
        updateInput.setSectionId(2L);
        updateInput.setSemesterId(3L);
        updateInput.setStatus("ACTIVE");

        StudentDetailDTO mockDetail = new StudentDetailDTO();
        mockDetail.setId(25L);
        mockDetail.setName("Aarav Sharma Updated");

        when(mockStudentDao.findById(25L)).thenReturn(Optional.of(existing));
        when(mockStudentDao.existsByEnrollmentNoExcludingId("2026CS101", 25L)).thenReturn(false);
        when(mockLookupDao.findSectionById(2L)).thenReturn(Optional.of(new Section()));
        when(mockLookupDao.findSemesterById(3L)).thenReturn(Optional.of(new Semester(3L, 3, "Sem 3")));
        when(mockStudentDao.update(any())).thenReturn(true);
        when(mockStudentDao.findDetailById(25L)).thenReturn(Optional.of(mockDetail));

        StudentDetailDTO result = studentService.updateStudent(updateInput, "faculty_user");

        assertNotNull(result);
        assertEquals("Aarav Sharma Updated", result.getName());
        verify(mockStudentDao).update(any());
        verify(mockAuditPublisher).publish(eq("STUDENT_UPDATE"), eq("faculty_user"), eq("STUDENT"), eq(25L), anyString());
    }

    @Test
    @DisplayName("Reject update when another student has the same enrollment number")
    void testUpdateDuplicateEnrollmentConflictThrows() throws Exception {
        Student existing = new Student();
        existing.setId(25L);
        existing.setEnrollmentNo("2026CS101");

        Student updateInput = new Student();
        updateInput.setId(25L);
        updateInput.setEnrollmentNo("2026EC999");
        updateInput.setName("Name");
        updateInput.setSectionId(1L);
        updateInput.setSemesterId(1L);

        when(mockStudentDao.findById(25L)).thenReturn(Optional.of(existing));
        when(mockStudentDao.existsByEnrollmentNoExcludingId("2026EC999", 25L)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                studentService.updateStudent(updateInput, "faculty")
        );
        assertTrue(ex.getMessage().contains("Another student with enrollment number"));
        verify(mockStudentDao, never()).update(any());
    }

    @Test
    @DisplayName("Toggle student active status successfully")
    void testToggleStatusSuccess() throws Exception {
        Student existing = new Student();
        existing.setId(10L);
        existing.setStatus("ACTIVE");

        when(mockStudentDao.findById(10L)).thenReturn(Optional.of(existing));
        when(mockStudentDao.updateStatus(10L, "INACTIVE")).thenReturn(true);

        boolean toggled = studentService.toggleStatus(10L, "admin");

        assertTrue(toggled);
        verify(mockStudentDao).updateStatus(10L, "INACTIVE");
        verify(mockAuditPublisher).publish(eq("STUDENT_STATUS_TOGGLE"), eq("admin"), eq("STUDENT"), eq(10L), anyString());
    }

    @Test
    @DisplayName("List students with filter returns valid Page container")
    void testListStudents() throws Exception {
        StudentFilter filter = new StudentFilter().setPage(1).setPageSize(10);
        StudentDetailDTO s1 = new StudentDetailDTO();
        s1.setId(1L);
        s1.setName("Alice");

        when(mockStudentDao.countWithFilter(filter)).thenReturn(1L);
        when(mockStudentDao.findWithFilter(filter)).thenReturn(List.of(s1));

        Page<StudentDetailDTO> page = studentService.listStudents(filter);

        assertNotNull(page);
        assertEquals(1, page.getTotalItems());
        assertEquals(1, page.getItems().size());
        assertEquals("Alice", page.getItems().get(0).getName());
    }

    @Test
    @DisplayName("Instant search returns limited matched students")
    void testInstantSearch() throws Exception {
        StudentDetailDTO s1 = new StudentDetailDTO();
        s1.setEnrollmentNo("2026CS101");
        when(mockStudentDao.searchInstant("CS10", 10)).thenReturn(List.of(s1));

        List<StudentDetailDTO> results = studentService.instantSearch("CS10", 10);
        assertEquals(1, results.size());
        assertEquals("2026CS101", results.get(0).getEnrollmentNo());
    }
}
