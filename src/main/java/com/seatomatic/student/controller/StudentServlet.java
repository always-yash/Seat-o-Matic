package com.seatomatic.student.controller;

import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.common.security.CsrfTokenManager;
import com.seatomatic.common.security.Role;
import com.seatomatic.common.web.BaseController;
import com.seatomatic.student.model.Branch;
import com.seatomatic.student.model.Section;
import com.seatomatic.student.model.Semester;
import com.seatomatic.student.model.Student;
import com.seatomatic.student.model.dto.BatchGroupStatsDTO;
import com.seatomatic.student.model.dto.Page;
import com.seatomatic.student.model.dto.StudentDetailDTO;
import com.seatomatic.student.model.dto.StudentEligibilityDTO;
import com.seatomatic.student.model.dto.StudentFilter;
import com.seatomatic.student.service.StudentEligibilityService;
import com.seatomatic.student.service.StudentImportService;
import com.seatomatic.student.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Primary HTTP controller for student management: roster presentation,
 * dynamic search filters, student registration, profile updates, and CSV exports.
 */
@WebServlet(name = "StudentServlet", urlPatterns = {"/students", "/students/*"})
public class StudentServlet extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentServlet.class);

    private final StudentService studentService;
    private final StudentImportService importService;
    private final StudentEligibilityService eligibilityService;

    public StudentServlet() {
        this(new StudentService(), new StudentImportService(), new StudentEligibilityService());
    }

    public StudentServlet(StudentService studentService,
                          StudentImportService importService,
                          StudentEligibilityService eligibilityService) {
        this.studentService = studentService != null ? studentService : new StudentService();
        this.importService = importService != null ? importService : new StudentImportService();
        this.eligibilityService = eligibilityService != null ? eligibilityService : new StudentEligibilityService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireRole(request, response, Role.ADMIN, Role.FACULTY)) {
            return;
        }

        String action = parameter(request, "action", "list");

        try {
            switch (action.toLowerCase()) {
                case "new":
                case "create":
                    handleNewStudentForm(request, response);
                    break;
                case "edit":
                    handleEditStudentForm(request, response);
                    break;
                case "view":
                case "detail":
                    handleViewStudentDetail(request, response);
                    break;
                case "search":
                    handleInstantSearch(request, response);
                    break;
                case "sections":
                    handleLoadSections(request, response);
                    break;
                case "export":
                    handleExportCsv(request, response);
                    break;
                case "template":
                    handleDownloadTemplate(request, response);
                    break;
                case "list":
                default:
                    handleListStudents(request, response);
                    break;
            }
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireRole(request, response, Role.ADMIN, Role.FACULTY)) {
            return;
        }

        String action = parameter(request, "action");
        String actor = currentUsername(request);

        try {
            switch (action.toLowerCase()) {
                case "create":
                    handleCreateStudent(request, response, actor);
                    break;
                case "update":
                    handleUpdateStudent(request, response, actor);
                    break;
                case "toggle-status":
                    handleToggleStatus(request, response, actor);
                    break;
                case "delete":
                    handleDeleteStudent(request, response, actor);
                    break;
                default:
                    setFlashError(request, "Unrecognized student action: " + action);
                    redirect(request, response, "/students");
                    break;
            }
        } catch (ValidationException ex) {
            LOGGER.warn("Validation failure in StudentServlet: {}", ex.getMessage());
            setFlashError(request, ex.getMessage());
            redirect(request, response, "/students");
        } catch (Exception ex) {
            handleException(request, response, ex);
        }
    }

    // ==========================================
    // GET Action Handlers
    // ==========================================

    private void handleListStudents(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        StudentFilter filter = new StudentFilter()
                .setKeyword(parameter(request, "q", parameter(request, "keyword")))
                .setBranchId(longParameter(request, "branchId"))
                .setSectionId(longParameter(request, "sectionId"))
                .setSemesterId(longParameter(request, "semesterId"))
                .setStatus(parameter(request, "status"))
                .setPage(intParameter(request, "page", 1))
                .setPageSize(intParameter(request, "pageSize", StudentFilter.DEFAULT_PAGE_SIZE))
                .setSortBy(parameter(request, "sortBy", "enrollment_no"))
                .setSortOrder(parameter(request, "sortOrder", "ASC"));

        Page<StudentDetailDTO> studentsPage = studentService.listStudents(filter);
        List<Branch> branches = studentService.getAllBranches();
        List<Semester> semesters = studentService.getAllSemesters();
        List<BatchGroupStatsDTO> batchStats = studentService.getBatchStatistics();
        long totalStudents = studentService.getTotalStudentCount();
        long activeStudents = studentService.getActiveStudentCount();

        List<Section> sections = List.of();
        if (filter.getBranchId() != null) {
            sections = studentService.getSectionsByBranch(filter.getBranchId());
        }

        request.setAttribute("studentsPage", studentsPage);
        request.setAttribute("filter", filter);
        request.setAttribute("branches", branches);
        request.setAttribute("sections", sections);
        request.setAttribute("semesters", semesters);
        request.setAttribute("batchStats", batchStats);
        request.setAttribute("totalStudents", totalStudents);
        request.setAttribute("activeStudents", activeStudents);

        attachCsrf(request);
        render(request, response, "student/students.jsp");
    }

    private void handleNewStudentForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        request.setAttribute("student", new Student());
        request.setAttribute("isEdit", false);
        request.setAttribute("branches", studentService.getAllBranches());
        request.setAttribute("semesters", studentService.getAllSemesters());
        request.setAttribute("sections", List.of());

        attachCsrf(request);
        render(request, response, "student/student-form.jsp");
    }

    private void handleEditStudentForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ValidationException, ServletException, IOException {
        Long id = requireLongParameter(request, "id");
        StudentDetailDTO student = studentService.getStudentDetail(id)
                .orElseThrow(() -> new ValidationException("Student not found with ID: " + id));

        request.setAttribute("student", student);
        request.setAttribute("isEdit", true);
        request.setAttribute("branches", studentService.getAllBranches());
        request.setAttribute("semesters", studentService.getAllSemesters());
        if (student.getBranchId() != null) {
            request.setAttribute("sections", studentService.getSectionsByBranch(student.getBranchId()));
        } else {
            request.setAttribute("sections", List.of());
        }

        attachCsrf(request);
        render(request, response, "student/student-form.jsp");
    }

    private void handleViewStudentDetail(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ValidationException, ServletException, IOException {
        Long id = requireLongParameter(request, "id");
        StudentDetailDTO student = studentService.getStudentDetail(id)
                .orElseThrow(() -> new ValidationException("Student not found with ID: " + id));

        List<StudentEligibilityDTO> eligibleExams = eligibilityService.getEligibleExamsForStudent(id);

        request.setAttribute("student", student);
        request.setAttribute("eligibleExams", eligibleExams);

        attachCsrf(request);
        render(request, response, "student/student-detail.jsp");
    }

    private void handleInstantSearch(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String query = parameter(request, "q");
        int limit = intParameter(request, "limit", 10);
        List<StudentDetailDTO> results = studentService.instantSearch(query, limit);
        sendJsonSuccess(response, results);
    }

    private void handleLoadSections(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Long branchId = longParameter(request, "branchId");
        if (branchId == null) {
            sendJsonSuccess(response, List.of());
            return;
        }
        List<Section> sections = studentService.getSectionsByBranch(branchId);
        sendJsonSuccess(response, sections);
    }

    private void handleExportCsv(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        StudentFilter filter = new StudentFilter()
                .setKeyword(parameter(request, "q", parameter(request, "keyword")))
                .setBranchId(longParameter(request, "branchId"))
                .setSectionId(longParameter(request, "sectionId"))
                .setSemesterId(longParameter(request, "semesterId"))
                .setStatus(parameter(request, "status"))
                .setPage(1)
                .setPageSize(5000); // large export batch

        Page<StudentDetailDTO> page = studentService.listStudents(filter);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"seatomatic-students.csv\"");
        try (OutputStream os = response.getOutputStream()) {
            importService.exportToCsv(page.getItems(), os);
        }
    }

    private void handleDownloadTemplate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        byte[] template = importService.generateCsvTemplate();
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"seatomatic-student-template.csv\"");
        response.setContentLength(template.length);
        try (OutputStream os = response.getOutputStream()) {
            os.write(template);
            os.flush();
        }
    }

    // ==========================================
    // POST Action Handlers
    // ==========================================

    private void handleCreateStudent(HttpServletRequest request, HttpServletResponse response, String actor)
            throws SQLException, ValidationException, IOException {
        Student student = new Student();
        student.setEnrollmentNo(requireParameter(request, "enrollmentNo"));
        student.setName(requireParameter(request, "name"));
        student.setSectionId(requireLongParameter(request, "sectionId"));
        student.setSemesterId(requireLongParameter(request, "semesterId"));
        student.setStatus(parameter(request, "status", "ACTIVE"));

        StudentDetailDTO created = studentService.registerStudent(student, actor);
        setFlashSuccess(request, "Successfully registered student " + created.getName() + " (" + created.getEnrollmentNo() + ").");
        redirect(request, response, "/students");
    }

    private void handleUpdateStudent(HttpServletRequest request, HttpServletResponse response, String actor)
            throws SQLException, ValidationException, IOException {
        Long id = requireLongParameter(request, "id");
        Student student = new Student();
        student.setId(id);
        student.setEnrollmentNo(requireParameter(request, "enrollmentNo"));
        student.setName(requireParameter(request, "name"));
        student.setSectionId(requireLongParameter(request, "sectionId"));
        student.setSemesterId(requireLongParameter(request, "semesterId"));
        student.setStatus(parameter(request, "status", "ACTIVE"));

        StudentDetailDTO updated = studentService.updateStudent(student, actor);
        setFlashSuccess(request, "Successfully updated profile for " + updated.getName() + " (" + updated.getEnrollmentNo() + ").");
        redirect(request, response, "/students?action=view&id=" + id);
    }

    private void handleToggleStatus(HttpServletRequest request, HttpServletResponse response, String actor)
            throws SQLException, ValidationException, IOException {
        Long id = requireLongParameter(request, "id");
        studentService.toggleStatus(id, actor);
        setFlashSuccess(request, "Student enrollment status changed successfully.");
        String redirectTarget = parameter(request, "redirect", "/students");
        redirect(request, response, redirectTarget);
    }

    private void handleDeleteStudent(HttpServletRequest request, HttpServletResponse response, String actor)
            throws SQLException, ValidationException, IOException {
        Long id = requireLongParameter(request, "id");
        studentService.deleteStudent(id, actor);
        setFlashSuccess(request, "Student record deleted successfully.");
        redirect(request, response, "/students");
    }

    private void attachCsrf(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String token = CsrfTokenManager.token(session);
            if (token == null) {
                token = CsrfTokenManager.generateToken(session);
            }
            request.setAttribute("csrfToken", token);
        }
    }
}

