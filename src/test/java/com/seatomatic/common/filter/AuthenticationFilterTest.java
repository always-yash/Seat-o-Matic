package com.seatomatic.common.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AuthenticationFilterTest {

    private AuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        filter = new AuthenticationFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        session = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn("");
    }

    @Test
    void testPublicPathsBypassAuthentication() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void testStaticAssetsBypassAuthentication() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/assets/css/main.css");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void testStaticExtensionBypass() {
        assertTrue(AuthenticationFilter.isPublicPath("/some/path/bundle.js"));
        assertTrue(AuthenticationFilter.isPublicPath("/style.css"));
        assertTrue(AuthenticationFilter.isPublicPath("/logo.svg"));
        assertTrue(AuthenticationFilter.isPublicPath("/font.woff2"));
        assertTrue(AuthenticationFilter.isPublicPath("/index.jsp"));
        assertTrue(AuthenticationFilter.isPublicPath("/"));
        assertFalse(AuthenticationFilter.isPublicPath("/dashboard"));
        assertFalse(AuthenticationFilter.isPublicPath("/students"));
    }

    @Test
    void testAuthenticatedUserAtLoginRedirectsToDashboard() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("username")).thenReturn("admin");
        when(session.getAttribute("userRole")).thenReturn("ADMIN");

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/dashboard");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testUnauthenticatedUserRedirectedToLogin() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testUnauthenticatedAjaxRequestReturns401() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/students");
        when(request.getSession(false)).thenReturn(null);
        when(request.getHeader("Accept")).thenReturn("application/json");

        StringWriter out = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, never()).sendRedirect(anyString());
        assertTrue(out.toString().contains("Authentication required"));
    }

    @Test
    void testInvalidRoleStringDoesNotThrowException() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("username")).thenReturn("hacker");
        when(session.getAttribute("userRole")).thenReturn("NON_EXISTENT_ROLE");

        // Must not throw IllegalArgumentException, must redirect to login
        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testNullRoleStringDoesNotThrowException() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("username")).thenReturn("user");
        when(session.getAttribute("userRole")).thenReturn(null);
        when(session.getAttribute("role")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testValidSessionPermitted() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(10L);
        when(session.getAttribute("username")).thenReturn("prof_smith");
        when(session.getAttribute("userRole")).thenReturn("FACULTY");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }
}

