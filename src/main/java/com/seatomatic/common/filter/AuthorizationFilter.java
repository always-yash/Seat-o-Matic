package com.seatomatic.common.filter;

import com.seatomatic.common.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filter enforcing Role-Based Access Control (RBAC) across application routes.
 * Governs privileges for ADMIN, FACULTY, and INVIGILATOR tiers.
 * Protects against exceptions when encountering null or unrecognized role strings.
 */
public class AuthorizationFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = AuthenticationFilter.requestPath(httpRequest);

        // Bypass public paths, static assets, and auth lifecycle endpoints
        if (AuthenticationFilter.isPublicPath(path) || "/logout".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + AuthenticationFilter.LOGIN_PATH);
            return;
        }

        Role userRole = resolveRole(session);
        if (userRole == null) {
            LOGGER.warn("Access denied: session lacks valid role credential for path {}", path);
            handleForbidden(httpRequest, httpResponse, "Invalid or missing role credential");
            return;
        }

        if (!isAuthorized(path, userRole)) {
            LOGGER.warn("User with role {} unauthorized for path {}", userRole, path);
            handleForbidden(httpRequest, httpResponse, "Insufficient permissions to access this resource");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Safely resolves the active user role from session attributes without throwing exceptions.
     */
    public static Role resolveRole(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object roleAttr = session.getAttribute("userRole");
        if (roleAttr == null) {
            roleAttr = session.getAttribute("role");
        }
        if (roleAttr instanceof Role r) {
            return r;
        }
        if (roleAttr instanceof String roleStr) {
            return Role.fromString(roleStr);
        }
        return null;
    }

    /**
     * Determines whether the specified role is authorized for the target path.
     */
    public static boolean isAuthorized(String path, Role role) {
        if (role == null) {
            return false;
        }

        // ADMIN has unrestricted access across all application endpoints
        if (role == Role.ADMIN) {
            return true;
        }

        // Common authenticated routes accessible to all authenticated roles
        if (path.equals("/dashboard") || path.equals("/profile") || path.startsWith("/profile/")) {
            return true;
        }

        // Admin-only management endpoints
        if (path.startsWith("/admin") || path.startsWith("/users") || path.startsWith("/settings")) {
            return false;
        }

        // Audit-specific reports are restricted to ADMIN
        if (path.startsWith("/reports/audit") || path.startsWith("/audit")) {
            return false;
        }

        // FACULTY tier permissions
        if (role == Role.FACULTY) {
            // Faculty can manage students, exams, rooms, seating, papers, and view reports
            return path.startsWith("/students")
                    || path.startsWith("/exams")
                    || path.startsWith("/rooms")
                    || path.startsWith("/seating")
                    || path.startsWith("/papers")
                    || path.startsWith("/distribution")
                    || path.startsWith("/reports")
                    || path.startsWith("/invigilation")
                    || path.startsWith("/invigilator");
        }

        // INVIGILATOR tier permissions
        if (role == Role.INVIGILATOR) {
            // Invigilator can manage papers and examine seating/room status, but cannot edit master entities
            if (path.startsWith("/papers") || path.startsWith("/distribution")
                    || path.startsWith("/invigilation") || path.startsWith("/invigilator")) {
                return true;
            }
            if (path.startsWith("/seating/view") || path.startsWith("/seating/room")) {
                return true;
            }
            return false;
        }

        return false;
    }

    private void handleForbidden(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException, ServletException {
        if (AuthenticationFilter.isJsonRequest(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
    }
}

