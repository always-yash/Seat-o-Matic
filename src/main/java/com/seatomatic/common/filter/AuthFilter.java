package com.seatomatic.common.filter;

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

public class AuthFilter implements Filter {
    private static final String LOGIN_PATH = "/login";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = requestPath(httpRequest);

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null || !hasAuthenticatedUser(session)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + LOGIN_PATH);
            return;
        }

        chain.doFilter(request, response);
    }

    static String requestPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        return contextPath == null || contextPath.isEmpty() ? uri : uri.substring(contextPath.length());
    }

    static boolean isPublicPath(String path) {
        return path.equals(LOGIN_PATH)
                || path.equals("/")
                || path.equals("/index.jsp")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/assets/")
                || path.startsWith("/public/");
    }

    static boolean hasAuthenticatedUser(HttpSession session) {
        Object userId = session.getAttribute("userId");
        Object username = session.getAttribute("username");
        Object role = session.getAttribute("userRole");
        return userId != null && username instanceof String usernameValue && !usernameValue.isBlank()
                && role instanceof String roleValue && !roleValue.isBlank();
    }
}
