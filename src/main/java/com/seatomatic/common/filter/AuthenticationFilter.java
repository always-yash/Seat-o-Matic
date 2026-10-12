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
import java.util.Locale;
import java.util.Set;

/**
 * Filter enforcing user authentication across protected application routes.
 * Bypasses public endpoints, static assets, and safe error resources without throwing
 * exceptions on null or malformed session attributes.
 */
public class AuthenticationFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    public static final String LOGIN_PATH = "/login";
    public static final String DASHBOARD_PATH = "/dashboard";

    private static final Set<String> STATIC_EXTENSIONS = Set.of(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico",
            ".woff", ".woff2", ".ttf", ".eot", ".map", ".webp"
    );

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = requestPath(httpRequest);

        // Check if route is public or static asset
        if (isPublicPath(path)) {
            // If authenticated user navigates to /login, redirect directly to dashboard
            if (path.equals(LOGIN_PATH) && isAuthenticated(httpRequest.getSession(false))) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + DASHBOARD_PATH);
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // Validate session for protected routes
        HttpSession session = httpRequest.getSession(false);
        if (!isAuthenticated(session)) {
            LOGGER.debug("Unauthenticated access attempt to {}", path);
            if (isJsonRequest(httpRequest)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"success\":false,\"message\":\"Authentication required\"}");
                return;
            }
            httpResponse.sendRedirect(httpRequest.getContextPath() + LOGIN_PATH);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Extracts normalized path relative to the servlet context.
     */
    public static String requestPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        if (contextPath == null || contextPath.isEmpty()) {
            return uri;
        }
        return uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
    }

    /**
     * Determines whether the requested path is exempt from authentication.
     */
    public static boolean isPublicPath(String path) {
        if (path == null || path.isEmpty() || "/".equals(path) || "/index.jsp".equals(path)) {
            return true;
        }

        if (path.equals(LOGIN_PATH) || path.startsWith("/login/")) {
            return true;
        }

        if (path.startsWith("/assets/") || path.startsWith("/css/")
                || path.startsWith("/js/") || path.startsWith("/public/")
                || path.startsWith("/images/") || path.startsWith("/fonts/")
                || "/favicon.ico".equals(path)) {
            return true;
        }

        if (path.startsWith("/WEB-INF/views/error/") || path.startsWith("/error/")) {
            return true;
        }

        // Check static file extensions
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex != -1) {
            String extension = path.substring(lastDotIndex).toLowerCase(Locale.ROOT);
            if (STATIC_EXTENSIONS.contains(extension)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the session has valid, authenticated user attributes without throwing
     * IllegalArgumentException on invalid or null role strings.
     */
    public static boolean isAuthenticated(HttpSession session) {
        if (session == null) {
            return false;
        }
        Object userId = session.getAttribute("userId");
        Object username = session.getAttribute("username");
        if (userId == null || !(username instanceof String userStr) || userStr.isBlank()) {
            return false;
        }

        // Extract role safely without throwing IllegalArgumentException
        Object roleAttr = session.getAttribute("userRole");
        if (roleAttr == null) {
            roleAttr = session.getAttribute("role");
        }
        if (!(roleAttr instanceof String roleStr)) {
            return false;
        }

        Role role = Role.fromString(roleStr);
        return role != null;
    }

    /**
     * Detects if the incoming request expects a JSON response.
     */
    protected static boolean isJsonRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String acceptHeader = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        String uri = request.getRequestURI();
        return (acceptHeader != null && acceptHeader.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (uri != null && uri.contains("/api/"));
    }
}
