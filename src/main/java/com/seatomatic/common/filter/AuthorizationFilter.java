package com.seatomatic.common.filter;

import com.seatomatic.common.security.Role;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "authorizationFilter", urlPatterns = "/*")
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        String uri = httpRequest.getRequestURI();
        String roleName = String.valueOf(session.getAttribute("role"));
        Role role = Role.valueOf(roleName.toUpperCase());

        boolean allowed = true;
        if (uri.contains("/students") || uri.contains("/exams") || uri.contains("/rooms") || uri.contains("/reports")) {
            allowed = role == Role.ADMIN || role == Role.FACULTY;
        }

        if (!allowed) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        chain.doFilter(request, response);
    }
}
