package com.seatomatic.common.filter;

import com.seatomatic.common.security.Role;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

public class AuthorizationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = AuthFilter.requestPath(httpRequest);

        if (AuthFilter.isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        Role role = session == null ? null : parseRole(session.getAttribute("userRole"));
        if (role == null || !AuthFilter.hasAuthenticatedUser(session)) {
            if (session != null) {
                session.invalidate();
            }
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        if (requiresStaffRole(path) && role == Role.INVIGILATOR) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "This role cannot access the requested resource.");
            return;
        }
        chain.doFilter(request, response);
    }

    public static Role parseRole(Object roleValue) {
        if (!(roleValue instanceof String roleString) || roleString.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(roleString.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean requiresStaffRole(String path) {
        return path.startsWith("/students") || path.startsWith("/exams") || path.startsWith("/rooms")
                || path.startsWith("/reports") || path.startsWith("/seating") || path.startsWith("/papers");
    }
}
