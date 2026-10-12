package com.seatomatic.core.auth;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginServletTest {

    private AuthService mockAuthService;
    private AuditPublisher mockAuditPublisher;
    private LoginServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(AuthService.class);
        mockAuditPublisher = mock(AuditPublisher.class);
        servlet = new LoginServlet(mockAuthService, mockAuditPublisher);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    void testDoGetRendersLoginWhenNotAuthenticated() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getSession(true)).thenReturn(session);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("csrfToken"), anyString());
        verify(request).getRequestDispatcher("/WEB-INF/views/login.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGetRedirectsToDashboardWhenAlreadyAuthenticated() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("username")).thenReturn("admin");
        when(session.getAttribute("userRole")).thenReturn("ADMIN");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/dashboard");
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    void testDoPostEmptyCredentialsDisplaysError() throws ServletException, IOException {
        when(request.getParameter("username")).thenReturn("");
        when(request.getParameter("password")).thenReturn("");
        when(request.getSession(true)).thenReturn(session);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), eq("Username and password are required."));
        verify(dispatcher).forward(request, response);
        verifyNoInteractions(mockAuthService);
    }

    @Test
    void testDoPostInvalidCredentialsAuditedAndRejected() throws Exception {
        when(request.getParameter("username")).thenReturn("admin");
        when(request.getParameter("password")).thenReturn("wrongpassword");
        when(request.getSession(true)).thenReturn(session);
        when(mockAuthService.authenticate("admin", "wrongpassword")).thenReturn(null);

        servlet.doPost(request, response);

        verify(mockAuditPublisher).publish(eq("LOGIN_FAILURE"), eq("admin"), eq("USER"), isNull(), anyString());
        verify(request).setAttribute(eq("errorMessage"), eq("Invalid username or password."));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPostSuccessfulAdminLogin() throws Exception {
        User adminUser = new User(1L, "admin", "System Administrator", "hashed", Role.ADMIN, true);

        when(request.getParameter("username")).thenReturn("admin");
        when(request.getParameter("password")).thenReturn("admin123");
        when(request.getSession(false)).thenReturn(session);
        when(mockAuthService.authenticate("admin", "admin123")).thenReturn(adminUser);

        servlet.doPost(request, response);

        verify(request).changeSessionId();
        verify(session).setAttribute("userId", 1L);
        verify(session).setAttribute("username", "admin");
        verify(session).setAttribute("userRole", "ADMIN");
        verify(mockAuditPublisher).publish(eq("LOGIN_SUCCESS"), eq("admin"), eq("USER"), eq(1L), anyString());
        verify(response).sendRedirect("/dashboard");
    }

    @Test
    void testDoPostSqlExceptionHandlesGracefully() throws Exception {
        when(request.getParameter("username")).thenReturn("admin");
        when(request.getParameter("password")).thenReturn("admin123");
        when(request.getSession(true)).thenReturn(session);
        when(mockAuthService.authenticate(anyString(), anyString())).thenThrow(new SQLException("DB connection failure"));

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), eq("Sign-in is temporarily unavailable. Please try again."));
        verify(dispatcher).forward(request, response);
    }
}

