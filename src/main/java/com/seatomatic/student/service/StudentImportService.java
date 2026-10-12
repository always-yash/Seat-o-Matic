package com.seatomatic.student.service;

import com.google.gson.Gson;
import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.report.audit.AuditPublisherImpl;
import com.seatomatic.student.dao.AcademicLookupDao;
import com.seatomatic.student.dao.StudentDao;
import com.seatomatic.student.model.Branch;
import com.seatomatic.student.model.Section;
import com.seatomatic.student.model.Semester;
import com.seatomatic.student.model.Student;
import com.seatomatic.student.model.dto.BatchImportResult;
import com.seatomatic.student.model.dto.StudentDetailDTO;
import com.seatomatic.student.model.dto.StudentFilter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * High-performance CSV import and export service for student rosters.
 * Implements defensive data scrubbing, pre-flight duplicate checking,
 * dynamic academic branch/section resolution, and audit logging.
 */
public class StudentImportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentImportService.class);
    private static final Pattern ENROLLMENT_PATTERN = Pattern.compile("^[A-Za-z0-9/_-]{3,50}$");
    private static final Gson GSON = new Gson();

    private final StudentDao studentDao;
    private final AcademicLookupDao academicLookupDao;
    private final AuditPublisher auditPublisher;

    public StudentImportService() {
        this(new StudentDao(), new AcademicLookupDao(), new AuditPublisherImpl());
    }

    public StudentImportService(StudentDao studentDao, AcademicLookupDao academicLookupDao, AuditPublisher auditPublisher) {
        this.studentDao = studentDao;
        this.academicLookupDao = academicLookupDao;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Ingests and registers students from an uploaded CSV stream.
     */
    public BatchImportResult importFromCsv(InputStream inputStream, String actorUsername) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream must not be null");
        }

        BatchImportResult result = new BatchImportResult();
        Set<String> seenInFile = new HashSet<>();
        List<Student> studentsToInsert = new ArrayList<>();

        // Cache academic lookups to avoid repeat queries
        Map<String, Branch> branchCache = new HashMap<>();
        Map<String, Section> sectionCache = new HashMap<>();
        Map<Integer, Semester> semesterCache = new HashMap<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .setIgnoreEmptyLines(true)
                    .build();

            try (CSVParser parser = new CSVParser(reader, format)) {
                int rowNumber = 1; // 1 represents the first data row after header

                for (CSVRecord record : parser) {
                    rowNumber++;
                    result.setTotalRows(result.getTotalRows() + 1);

                    String enrollmentNo = extractColumn(record, "enrollment_no", "enrollment", "roll_no", "rollno", "roll");
                    String name = extractColumn(record, "name", "student_name", "full_name");
                    String branchCode = extractColumn(record, "branch_code", "branch", "dept", "department");
                    String sectionName = extractColumn(record, "section", "section_name", "sec");
                    String semesterStr = extractColumn(record, "semester", "sem", "semester_number");
                    String statusStr = extractColumn(record, "status");

                    // Validate enrollment number
                    if (enrollmentNo == null || enrollmentNo.isBlank()) {
                        result.addError(rowNumber, enrollmentNo, "Roll number / Enrollment number is missing.");
                        continue;
                    }
                    enrollmentNo = enrollmentNo.trim().toUpperCase();
                    if (!ENROLLMENT_PATTERN.matcher(enrollmentNo).matches()) {
                        result.addError(rowNumber, enrollmentNo, "Roll number contains invalid characters (3-50 alphanumeric permitted).");
                        continue;
                    }

                    // Check for duplicate in current upload
                    if (!seenInFile.add(enrollmentNo)) {
                        result.addError(rowNumber, enrollmentNo, "Duplicate enrollment number within the same CSV upload.");
                        continue;
                    }

                    // Check for duplicate in database
                    try {
                        if (studentDao.existsByEnrollmentNo(enrollmentNo)) {
                            result.incrementSkipped();
                            result.addError(rowNumber, enrollmentNo, "Student is already registered in database (skipped).");
                            continue;
                        }
                    } catch (SQLException e) {
                        LOGGER.error("DB error checking existence for enrollment: {}", enrollmentNo, e);
                        result.addError(rowNumber, enrollmentNo, "Database error during duplicate check: " + e.getMessage());
                        continue;
                    }

                    // Validate Name
                    if (name == null || name.isBlank() || name.trim().length() < 2) {
                        result.addError(rowNumber, enrollmentNo, "Student name is invalid or shorter than 2 characters.");
                        continue;
                    }

                    // Resolve Branch
                    if (branchCode == null || branchCode.isBlank()) {
                        branchCode = "GEN"; // Default general branch fallback
                    }
                    branchCode = branchCode.trim().toUpperCase();
                    Branch branch;
                    try {
                        branch = resolveBranch(branchCode, branchCache);
                    } catch (SQLException e) {
                        result.addError(rowNumber, enrollmentNo, "Failed to resolve academic branch '" + branchCode + "': " + e.getMessage());
                        continue;
                    }

                    // Resolve Section
                    if (sectionName == null || sectionName.isBlank()) {
                        sectionName = "Section A";
                    }
                    Section section;
                    try {
                        section = resolveSection(branch.getId(), sectionName.trim(), sectionCache);
                    } catch (SQLException e) {
                        result.addError(rowNumber, enrollmentNo, "Failed to resolve section '" + sectionName + "': " + e.getMessage());
                        continue;
                    }

                    // Resolve Semester
                    int semNumber = parseSemesterNumber(semesterStr);
                    Semester semester;
                    try {
                        semester = resolveSemester(semNumber, semesterCache);
                    } catch (SQLException e) {
                        result.addError(rowNumber, enrollmentNo, "Failed to resolve semester '" + semNumber + "': " + e.getMessage());
                        continue;
                    }

                    // Build student entity
                    Student student = new Student();
                    student.setEnrollmentNo(enrollmentNo);
                    student.setName(name.trim());
                    student.setSectionId(section.getId());
                    student.setSemesterId(semester.getId());
                    student.setStatus(("INACTIVE".equalsIgnoreCase(statusStr)) ? "INACTIVE" : "ACTIVE");

                    studentsToInsert.add(student);
                    result.incrementImported();
                }
            }
        }

        // Execute batch insertion
        if (!studentsToInsert.isEmpty()) {
            try {
                studentDao.batchInsert(studentsToInsert);
                LOGGER.info("CSV batch import executed: inserted {} students", studentsToInsert.size());

                if (auditPublisher != null) {
                    Map<String, Object> details = Map.of(
                            "totalProcessed", result.getTotalRows(),
                            "importedCount", result.getImportedCount(),
                            "skippedCount", result.getSkippedCount(),
                            "errorCount", result.getErrorCount()
                    );
                    auditPublisher.publish("STUDENT_BATCH_IMPORT", actorUsername != null ? actorUsername : "SYSTEM",
                            "STUDENT", null, GSON.toJson(details));
                }
            } catch (SQLException e) {
                LOGGER.error("Fatal database error during batch insert", e);
                throw new IOException("Failed to execute database batch insert: " + e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * Exports a list of students to CSV format writing directly into the given output stream.
     */
    public void exportToCsv(List<StudentDetailDTO> students, OutputStream outputStream) throws IOException {
        if (outputStream == null) {
            throw new IllegalArgumentException("OutputStream must not be null");
        }
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("Enrollment No", "Full Name", "Branch Code", "Branch Name", "Section", "Semester", "Status", "Registered At")
                     .build())) {

            if (students != null) {
                for (StudentDetailDTO s : students) {
                    printer.printRecord(
                            s.getEnrollmentNo(),
                            s.getName(),
                            s.getBranchCode() != null ? s.getBranchCode() : "",
                            s.getBranchName() != null ? s.getBranchName() : "",
                            s.getSectionName() != null ? s.getSectionName() : "",
                            s.getSemesterNumber() != null ? "Semester " + s.getSemesterNumber() : "",
                            s.getStatus() != null ? s.getStatus() : "ACTIVE",
                            s.getCreatedAt() != null ? s.getCreatedAt().toString() : ""
                    );
                }
            }
            printer.flush();
        }
    }

    /**
     * Generates a sample template CSV content for user download.
     */
    public byte[] generateCsvTemplate() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("enrollment_no", "name", "branch_code", "section", "semester", "status")
                     .build())) {

            printer.printRecord("2026CS101", "Aarav Sharma", "CSE", "Section A", "4", "ACTIVE");
            printer.printRecord("2026EC102", "Diya Patel", "ECE", "Section B", "4", "ACTIVE");
            printer.printRecord("2026ME103", "Rohan Verma", "MECH", "Section A", "6", "ACTIVE");
            printer.printRecord("2026IT104", "Ananya Iyer", "IT", "Section A", "2", "ACTIVE");
            printer.flush();
        }
        return baos.toByteArray();
    }

    // ==========================================
    // Resolution and extraction helpers
    // ==========================================

    private Branch resolveBranch(String branchCode, Map<String, Branch> cache) throws SQLException {
        if (cache.containsKey(branchCode)) {
            return cache.get(branchCode);
        }
        Branch branch = academicLookupDao.findOrCreateBranch(branchCode, branchCode + " Department");
        cache.put(branchCode, branch);
        return branch;
    }

    private Section resolveSection(Long branchId, String sectionName, Map<String, Section> cache) throws SQLException {
        String key = branchId + "#" + sectionName.toUpperCase();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Section section = academicLookupDao.findOrCreateSection(branchId, sectionName);
        cache.put(key, section);
        return section;
    }

    private Semester resolveSemester(int semNumber, Map<Integer, Semester> cache) throws SQLException {
        if (cache.containsKey(semNumber)) {
            return cache.get(semNumber);
        }
        Semester semester = academicLookupDao.findOrCreateSemester(semNumber, "Semester " + semNumber);
        cache.put(semNumber, semester);
        return semester;
    }

    private int parseSemesterNumber(String semesterStr) {
        if (semesterStr == null || semesterStr.isBlank()) {
            return 1;
        }
        String cleaned = semesterStr.replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) {
            return 1;
        }
        try {
            int num = Integer.parseInt(cleaned);
            return (num >= 1 && num <= 12) ? num : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String extractColumn(CSVRecord record, String... headerAliases) {
        for (String alias : headerAliases) {
            if (record.isMapped(alias)) {
                String val = record.get(alias);
                if (val != null && !val.isBlank()) {
                    return val.trim();
                }
            }
        }
        return null;
    }
}
