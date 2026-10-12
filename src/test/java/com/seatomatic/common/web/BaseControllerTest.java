package com.seatomatic.common.web;

import com.seatomatic.common.exception.SecurityException;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.common.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BaseControllerTest {

    private static class DummyController extends BaseController {
    }

    private DummyController controller;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        controller = new DummyController();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    void testParameterExtractionWithDefaults() {
        when(request.getParameter("key")).thenReturn("  hello  ");
        when(request.getParameter("missing")).thenReturn(null);
        when(request.getParameter("empty")).thenReturn("   ");

        assertEquals("hello", controller.parameter(request, "key"));
        assertEquals("defaultVal", controller.parameter(request, "missing", "defaultVal"));
        assertEquals("defaultVal", controller.parameter(request, "empty", "defaultVal"));
    }

    @Test
    void testRequireParameter() {
        when(request.getParameter("name")).thenReturn("SeatOMatic");
        when(request.getParameter("missing")).thenReturn(null);

        assertEquals("SeatOMatic", controller.requireParameter(request, "name"));
        assertThrows(ValidationException.class, () -> controller.requireParameter(request, "missing"));
    }

    @Test
    void testLongAndIntParameterExtraction() {
        when(request.getParameter("id")).thenReturn("123");
        when(request.getParameter("invalid")).thenReturn("abc");
        when(request.getParameter("missing")).thenReturn(null);

        assertEquals(123L, controller.longParameter(request, "id"));
        assertNull(controller.longParameter(request, "invalid"));
        assertEquals(99L, controller.longParameter(request, "missing", 99L));
        assertEquals(123, controller.intParameter(request, "id"));
        assertEquals(55, controller.intParameter(request, "invalid", 55));

        assertThrows(ValidationException.class, () -> controller.requireLongParameter(request, "invalid"));
        assertThrows(ValidationException.class, () -> controller.requireIntParameter(request, "missing"));
    }

    @Test
    void testBooleanParameter() {
        when(request.getParameter("b1")).thenReturn("true");
        when(request.getParameter("b2")).thenReturn("1");
        when(request.getParameter("b3")).thenReturn("no");
        when(request.getParameter("missing")).thenReturn(null);

        assertTrue(controller.booleanParameter(request, "b1"));
        assertTrue(controller.booleanParameter(request, "b2"));
        assertFalse(controller.booleanParameter(request, "b3"));
        assertTrue(controller.booleanParameter(request, "missing", true));
    }

    @Test
    void testJsonSuccessAndErrorResponses() throws IOException {
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        controller.sendJsonSuccess(response, "sample_data", "Operation completed");

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = sw.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"data\":\"sample_data\""));
        assertTrue(json.contains("\"message\":\"Operation completed\""));

        StringWriter swErr = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(swErr));

        controller.sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid input");
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(swErr.toString().contains("\"success\":false"));
        assertTrue(swErr.toString().contains("Invalid input"));
    }

    @Test
    void testSessionVerificationHelpers() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(42L);
        when(session.getAttribute("username")).thenReturn("admin_user");
        when(session.getAttribute("userRole")).thenReturn("ADMIN");

        assertEquals("admin_user", controller.currentUsername(request));
        assertEquals(42L, controller.currentUserId(request));
        assertEquals(Role.ADMIN, controller.currentRole(request));
        assertTrue(controller.isAuthenticated(request));
        assertTrue(controller.hasRole(request, Role.ADMIN));
        assertFalse(controller.hasRole(request, Role.FACULTY, Role.INVIGILATOR));
    }

    @Test
    void testRequireRoleEnforcement() throws IOException, ServletException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(42L);
        when(session.getAttribute("username")).thenReturn("faculty_user");
        when(session.getAttribute("userRole")).thenReturn("FACULTY");

        // Allowed role FACULTY
        assertTrue(controller.requireRole(request, response, Role.FACULTY, Role.ADMIN));

        // Prohibited role ADMIN-only
        assertFalse(controller.requireRole(request, response, Role.ADMIN));
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testFlashMessaging() {
        when(request.getSession(true)).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);

        Flash flash = Flash.success("Saved successfully");
        controller.setFlash(request, flash);
        verify(session).setAttribute(Flash.SESSION_KEY, flash);

        when(session.getAttribute(Flash.SESSION_KEY)).thenReturn(flash);
        Flash retrieved = controller.getFlash(request);
        assertEquals(flash, retrieved);
        verify(session).removeAttribute(Flash.SESSION_KEY);
    }

    @Test
    void testHandleExceptionRendersAppropriateStatus() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/dashboard");

        controller.handleException(request, response, new SecurityException("Forbidden access"));
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/error/403.jsp");

        controller.handleException(request, response, new RuntimeException("Crash"));
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(request).getRequestDispatcher("/WEB-INF/views/error/500.jsp");
    }
}

