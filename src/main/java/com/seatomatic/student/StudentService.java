package com.seatomatic.student;

import com.seatomatic.analysis.AuditService;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository students;
    private final AuditService audit;
    public StudentService(StudentRepository students, AuditService audit) { this.students = students; this.audit = audit; }
    @Transactional(readOnly = true) public List<Student> findAll() { return students.findAll(); }
    @Transactional(readOnly = true) public long count() { return students.count(); }
    @Transactional(readOnly = true) public Student findActive(String studentId) {
        Student student = students.findById(studentId).orElseThrow(() -> new IllegalArgumentException("Student not found."));
        if (!student.isActive()) throw new IllegalArgumentException("Inactive students cannot be registered for an examination.");
        return student;
    }
    @Transactional public Student create(CreateStudent input) {
        Student student = students.save(new Student(input.studentId(), input.fullName(), input.branch(), input.section(), input.semester()));
        audit.record("STUDENT_CREATED", "Student", student.getStudentId() + " · " + student.getFullName());
        return student;
    }

    @Transactional public ImportResult importCsv(String csv) {
        if (csv != null && csv.length() > 2 * 1024 * 1024) throw new IllegalArgumentException("CSV files must be 2 MB or smaller.");
        String[] lines = csv == null ? new String[0] : csv.replace("\uFEFF", "").split("\\R", -1);
        int dataRows = Math.max(0, lines.length - 1);
        if (lines.length > 1 && lines[lines.length - 1].isBlank()) dataRows--;
        if (dataRows > 5000) throw new IllegalArgumentException("CSV imports are limited to 5,000 data rows per file.");
        if (lines.length == 0 || !lines[0].strip().equalsIgnoreCase("studentId,fullName,branch,section,semester")) {
            throw new IllegalArgumentException("CSV header must be studentId,fullName,branch,section,semester.");
        }
        List<ImportError> errors = new ArrayList<>();
        List<Candidate> candidates = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int index = 1; index < lines.length; index++) {
            String line = lines[index].strip();
            if (line.isEmpty()) continue;
            int row = index + 1;
            List<String> columns;
            try { columns = parseCsvLine(line); }
            catch (IllegalArgumentException ex) { errors.add(new ImportError(row, ex.getMessage())); continue; }
            if (columns.size() != 5) { errors.add(new ImportError(row, "Expected exactly five columns.")); continue; }
            String id = columns.get(0).strip();
            String name = columns.get(1).strip();
            String branch = columns.get(2).strip();
            String section = columns.get(3).strip();
            String key = id.toLowerCase(java.util.Locale.ROOT);
            if (id.isEmpty() || name.isEmpty() || branch.isEmpty()) { errors.add(new ImportError(row, "Student ID, full name and branch are required.")); continue; }
            if (id.length() > 32 || name.length() > 120 || branch.length() > 100 || section.length() > 30) { errors.add(new ImportError(row, "One or more values exceed the allowed field length.")); continue; }
            int semester;
            try { semester = Integer.parseInt(columns.get(4).strip()); }
            catch (NumberFormatException ex) { errors.add(new ImportError(row, "Semester must be a number from 1 to 12.")); continue; }
            if (semester < 1 || semester > 12) { errors.add(new ImportError(row, "Semester must be a number from 1 to 12.")); continue; }
            if (!seen.add(key)) { errors.add(new ImportError(row, "Student ID appears more than once in this file.")); continue; }
            candidates.add(new Candidate(row, key, new Student(id, name, branch, section, semester)));
        }
        Set<String> candidateIds = candidates.stream().map(Candidate::key).collect(java.util.stream.Collectors.toSet());
        Set<String> existingIds = candidateIds.isEmpty() ? Set.of() : new HashSet<>(students.findExistingStudentIds(candidateIds));
        List<Student> accepted = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (existingIds.contains(candidate.key())) errors.add(new ImportError(candidate.row(), "Student ID already exists in the directory."));
            else accepted.add(candidate.student());
        }
        List<Student> saved = students.saveAll(accepted);
        if (!saved.isEmpty()) audit.record("STUDENTS_IMPORTED", "Student", saved.size() + " student records");
        return new ImportResult(saved.size(), errors.size(), errors);
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted && c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') { value.append('"'); i++; }
            else if (c == '"' && !quoted && value.length() == 0 && !closedQuote) quoted = true;
            else if (c == '"' && quoted) { quoted = false; closedQuote = true; }
            else if (c == ',' && !quoted) { values.add(value.toString()); value.setLength(0); closedQuote = false; }
            else if (!quoted && closedQuote) throw new IllegalArgumentException("Unexpected text after a quoted field.");
            else if (c == '"' && !quoted) throw new IllegalArgumentException("Quotes must wrap an entire field.");
            else value.append(c);
        }
        if (quoted) throw new IllegalArgumentException("Unclosed quoted field.");
        values.add(value.toString());
        return values;
    }

    public record ImportError(int row, String message) { }
    public record ImportResult(int imported, int rejected, List<ImportError> errors) { }
    private record Candidate(int row, String key, Student student) { }
    public record CreateStudent(String studentId, String fullName, String branch, String section, int semester) { }
}
