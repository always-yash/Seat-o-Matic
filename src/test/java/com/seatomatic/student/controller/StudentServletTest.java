package com.seatomatic.student.controller;

import com.seatomatic.common.security.Role;
import com.seatomatic.student.model.Student;
import com.seatomatic.student.model.dto.Page;
import com.seatomatic.student.model.dto.StudentDetailDTO;
import com.seatomatic.student.model.dto.StudentEligibilityDTO;
import com.seatomatic.student.service.StudentEligibilityService;
import com.seatomatic.student.service.StudentImportService;
import com.seatomatic.student.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Controller-level test suite for StudentServlet routing, security enforcement,
 * parameter parsing, and view rendering.
 */
class StudentServletTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private StudentService mockStudentService;
    private StudentImportService mockImportService;
    private StudentEligibilityService mockEligibilityService;
    private StudentServlet servlet;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(response.encodeRedirectURL(anyString())).thenAnswer(inv -> inv.getArgument(0));

        mockStudentService = mock(StudentService.class);
        mockImportService = mock(StudentImportService.class);
        mockEligibilityService = mock(StudentEligibilityService.class);

        servlet = new StudentServlet(mockStudentService, mockImportService, mockEligibilityService);
    }

    private void authenticateSession(Role role) {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("username")).thenReturn("tester");
        when(session.getAttribute("userRole")).thenReturn(role.name());
        when(session.getAttribute("role")).thenReturn(role.name());
    }

    @Test
    @DisplayName("Unauthenticated request redirects to /login")
    void testUnauthenticatedRedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/login");
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("Unauthorized role (INVIGILATOR) is forbidden (403)")
    void testUnauthorizedRoleForbidden() throws Exception {
        authenticateSession(Role.INVIGILATOR);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/error/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Authenticated ADMIN renders student roster view successfully")
    void testDoGetListStudentsSuccess() throws Exception {
        authenticateSession(Role.ADMIN);
        when(request.getParameter("action")).thenReturn("list");

        Page<StudentDetailDTO> mockPage = new Page<>(List.of(), 0, 1, 15);
        when(mockStudentService.listStudents(any())).thenReturn(mockPage);
        when(mockStudentService.getAllBranches()).thenReturn(List.of());
        when(mockStudentService.getAllSemesters()).thenReturn(List.of());
        when(mockStudentService.getBatchStatistics()).thenReturn(List.of());
        when(mockStudentService.getTotalStudentCount()).thenReturn(0L);
        when(mockStudentService.getActiveStudentCount()).thenReturn(0L);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("studentsPage"), eq(mockPage));
        verify(request).getRequestDispatcher("/WEB-INF/views/student/students.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet with action=new renders student registration form")
    void testDoGetNewStudentForm() throws Exception {
        authenticateSession(Role.FACULTY);
        when(request.getParameter("action")).thenReturn("new");
        when(mockStudentService.getAllBranches()).thenReturn(List.of());
        when(mockStudentService.getAllSemesters()).thenReturn(List.of());

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("isEdit"), eq(false));
        verify(request).getRequestDispatcher("/WEB-INF/views/student/student-form.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet with action=edit renders student edit form with populated record")
    void testDoGetEditStudentForm() throws Exception {
        authenticateSession(Role.FACULTY);
        when(request.getParameter("action")).thenReturn("edit");
        when(request.getParameter("id")).thenReturn("42");

        StudentDetailDTO detail = new StudentDetailDTO();
        detail.setId(42L);
        detail.setBranchId(1L);
        when(mockStudentService.getStudentDetail(42L)).thenReturn(Optional.of(detail));
        when(mockStudentService.getAllBranches()).thenReturn(List.of());
        when(mockStudentService.getAllSemesters()).thenReturn(List.of());
        when(mockStudentService.getSectionsByBranch(1L)).thenReturn(List.of());

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("isEdit"), eq(true));
        verify(request).setAttribute(eq("student"), eq(detail));
        verify(request).getRequestDispatcher("/WEB-INF/views/student/student-form.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet with action=view renders student profile and eligible examinations")
    void testDoGetViewStudentDetail() throws Exception {
        authenticateSession(Role.ADMIN);
        when(request.getParameter("action")).thenReturn("view");
        when(request.getParameter("id")).thenReturn("55");

        StudentDetailDTO detail = new StudentDetailDTO();
        detail.setId(55L);
        when(mockStudentService.getStudentDetail(55L)).thenReturn(Optional.of(detail));
        when(mockEligibilityService.getEligibleExamsForStudent(55L)).thenReturn(List.of(new StudentEligibilityDTO()));

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("student"), eq(detail));
        verify(request).setAttribute(eq("eligibleExams"), anyList());
        verify(request).getRequestDispatcher("/WEB-INF/views/student/student-detail.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet action=search returns instant JSON output")
    void testDoGetInstantSearchJson() throws Exception {
        authenticateSession(Role.ADMIN);
        when(request.getParameter("action")).thenReturn("search");
        when(request.getParameter("q")).thenReturn("2026CS");

        StudentDetailDTO match = new StudentDetailDTO();
        match.setEnrollmentNo("2026CS101");
        when(mockStudentService.instantSearch("2026CS", 10)).thenReturn(List.of(match));

        StringWriter out = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(request, response);

        verify(response).setContentType("application/json;charset=UTF-8");
        assertTrue(out.toString().contains("2026CS101"));
    }

    @Test
    @DisplayName("doPost action=create registers student and redirects to /students")
    void testDoPostCreateStudent() throws Exception {
        authenticateSession(Role.ADMIN);
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("enrollmentNo")).thenReturn("2026CS105");
        when(request.getParameter("name")).thenReturn("Rohan Verma");
        when(request.getParameter("sectionId")).thenReturn("1");
        when(request.getParameter("semesterId")).thenReturn("4");
        when(request.getParameter("status")).thenReturn("ACTIVE");

        StudentDetailDTO created = new StudentDetailDTO();
        created.setId(99L);
        created.setName("Rohan Verma");
        created.setEnrollmentNo("2026CS105");
        when(mockStudentService.registerStudent(any(Student.class), anyString())).thenReturn(created);

        servlet.doPost(request, response);

        verify(mockStudentService).registerStudent(any(Student.class), eq("tester"));
        verify(response).sendRedirect("/students");
    }

    @Test
    @DisplayName("doPost action=toggle-status toggles status and redirects")
    void testDoPostToggleStatus() throws Exception {
        authenticateSession(Role.ADMIN);
        when(request.getParameter("action")).thenReturn("toggle-status");
        when(request.getParameter("id")).thenReturn("88");

        servlet.doPost(request, response);

        verify(mockStudentService).toggleStatus(88L, "tester");
        verify(response).sendRedirect("/students");
    }
}

