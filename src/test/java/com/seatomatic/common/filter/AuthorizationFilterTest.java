package com.seatomatic.common.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AuthorizationFilterTest {

    private AuthorizationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        filter = new AuthorizationFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    void testPublicAndLogoutPathsBypass() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/logout");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void testAdminHasAccessToAllEndpoints() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/admin/settings");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("ADMIN");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void testFacultyPermittedForExamAndStudentRoutes() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/exams/schedule");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("FACULTY");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void testFacultyDeniedForAdminRoutes() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/admin/users");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("FACULTY");

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/error/403.jsp");
        verify(dispatcher).forward(request, response);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testFacultyDeniedForAuditReports() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/reports/audit");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("FACULTY");

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testInvigilatorPermittedForPapersAndInvigilation() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/papers/distribution");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("INVIGILATOR");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void testInvigilatorDeniedForStudentManagement() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/students/edit");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("INVIGILATOR");

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testInvalidRoleStringDoesNotThrowAndFailsClosed() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/students");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("UNRECOGNIZED_ROLE_123");

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testForbiddenAjaxReturnsJsonResponse() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/admin/api");
        when(request.getHeader("Accept")).thenReturn("application/json");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("INVIGILATOR");

        StringWriter out = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(out.toString().contains("Insufficient permissions"));
        verify(dispatcher, never()).forward(request, response);
    }
}

