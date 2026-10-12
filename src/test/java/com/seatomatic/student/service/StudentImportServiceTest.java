package com.seatomatic.student.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.student.dao.AcademicLookupDao;
import com.seatomatic.student.dao.StudentDao;
import com.seatomatic.student.model.Branch;
import com.seatomatic.student.model.Section;
import com.seatomatic.student.model.Semester;
import com.seatomatic.student.model.dto.BatchImportResult;
import com.seatomatic.student.model.dto.StudentDetailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for bulk CSV roster ingestion, sanitization,
 * duplicate suppression, and roster exports.
 */
class StudentImportServiceTest {

    private StudentDao mockStudentDao;
    private AcademicLookupDao mockLookupDao;
    private AuditPublisher mockAuditPublisher;
    private StudentImportService importService;

    @BeforeEach
    void setUp() throws Exception {
        mockStudentDao = mock(StudentDao.class);
        mockLookupDao = mock(AcademicLookupDao.class);
        mockAuditPublisher = mock(AuditPublisher.class);

        Branch cseBranch = new Branch();
        cseBranch.setId(1L);
        cseBranch.setCode("CSE");

        Section secA = new Section();
        secA.setId(10L);
        secA.setBranchId(1L);
        secA.setName("Section A");

        Semester sem4 = new Semester(4L, 4, "Semester 4");

        when(mockLookupDao.findOrCreateBranch(anyString(), anyString())).thenReturn(cseBranch);
        when(mockLookupDao.findOrCreateSection(anyLong(), anyString())).thenReturn(secA);
        when(mockLookupDao.findOrCreateSemester(anyInt(), anyString())).thenReturn(sem4);

        importService = new StudentImportService(mockStudentDao, mockLookupDao, mockAuditPublisher);
    }

    @Test
    @DisplayName("Bulk import successfully registers clean CSV records")
    void testImportValidCsv() throws Exception {
        String csvData =
                "enrollment_no,name,branch_code,section,semester,status\n" +
                "2026CS101,Aarav Sharma,CSE,Section A,4,ACTIVE\n" +
                "2026CS102,Diya Patel,CSE,Section A,4,ACTIVE\n";

        when(mockStudentDao.existsByEnrollmentNo(anyString())).thenReturn(false);

        ByteArrayInputStream input = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        BatchImportResult result = importService.importFromCsv(input, "admin");

        assertNotNull(result);
        assertEquals(2, result.getTotalRows());
        assertEquals(2, result.getImportedCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals(0, result.getErrorCount());
        verify(mockStudentDao).batchInsert(anyList());
        verify(mockAuditPublisher).publish(eq("STUDENT_BATCH_IMPORT"), eq("admin"), eq("STUDENT"), isNull(), anyString());
    }

    @Test
    @DisplayName("Bulk import flags duplicate enrollment numbers within the same file")
    void testImportInFileDuplicateFlagged() throws Exception {
        String csvData =
                "enrollment_no,name,branch_code,section,semester\n" +
                "2026CS101,Aarav Sharma,CSE,Section A,4\n" +
                "2026CS101,Duplicate Person,CSE,Section A,4\n";

        when(mockStudentDao.existsByEnrollmentNo(anyString())).thenReturn(false);

        ByteArrayInputStream input = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        BatchImportResult result = importService.importFromCsv(input, "admin");

        assertEquals(2, result.getTotalRows());
        assertEquals(1, result.getImportedCount());
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).getMessage().contains("Duplicate enrollment number within the same CSV"));
    }

    @Test
    @DisplayName("Bulk import gracefully skips records already existing in the database")
    void testImportDatabaseDuplicateSkipped() throws Exception {
        String csvData =
                "enrollment_no,name,branch_code,section,semester\n" +
                "2026CS101,Existing Student,CSE,Section A,4\n";

        when(mockStudentDao.existsByEnrollmentNo("2026CS101")).thenReturn(true);

        ByteArrayInputStream input = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        BatchImportResult result = importService.importFromCsv(input, "admin");

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getImportedCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).getMessage().contains("already registered"));
    }

    @Test
    @DisplayName("Generate CSV import template with standard headers")
    void testGenerateCsvTemplate() throws Exception {
        byte[] templateBytes = importService.generateCsvTemplate();
        assertNotNull(templateBytes);
        String content = new String(templateBytes, StandardCharsets.UTF_8);
        assertTrue(content.contains("enrollment_no"));
        assertTrue(content.contains("branch_code"));
        assertTrue(content.contains("Aarav Sharma"));
    }

    @Test
    @DisplayName("Export student roster to CSV format")
    void testExportToCsv() throws Exception {
        StudentDetailDTO s1 = new StudentDetailDTO();
        s1.setEnrollmentNo("2026CS101");
        s1.setName("Aarav Sharma");
        s1.setBranchCode("CSE");
        s1.setBranchName("Computer Science");
        s1.setSectionName("Section A");
        s1.setSemesterNumber(4);
        s1.setStatus("ACTIVE");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        importService.exportToCsv(List.of(s1), baos);

        String output = baos.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Enrollment No"));
        assertTrue(output.contains("2026CS101"));
        assertTrue(output.contains("Aarav Sharma"));
        assertTrue(output.contains("Computer Science"));
    }
}
