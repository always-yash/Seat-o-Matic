package com.seatomatic.common.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seatomatic.common.exception.ErrorMapper;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.common.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Base abstract servlet providing centralized JSON formatting, parameter parsing,
 * session authentication & role verification helpers, and view forwarding.
 */
public abstract class BaseController extends HttpServlet {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    // ==========================================
    // Request Parameter Extraction Helpers
    // ==========================================

    protected String parameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    protected String parameter(HttpServletRequest request, String name, String defaultValue) {
        String value = parameter(request, name);
        return value.isEmpty() ? defaultValue : value;
    }

    protected String requireParameter(HttpServletRequest request, String name) throws ValidationException {
        String value = parameter(request, name);
        if (value.isEmpty()) {
            throw new ValidationException("Required parameter '" + name + "' is missing or empty.");
        }
        return value;
    }

    protected Long longParameter(HttpServletRequest request, String name) {
        return longParameter(request, name, null);
    }

    protected Long longParameter(HttpServletRequest request, String name, Long defaultValue) {
        String value = parameter(request, name);
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    protected Long requireLongParameter(HttpServletRequest request, String name) throws ValidationException {
        String value = parameter(request, name);
        if (value.isEmpty()) {
            throw new ValidationException("Required numeric parameter '" + name + "' is missing.");
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Parameter '" + name + "' must be a valid integer number.");
        }
    }

    protected Integer intParameter(HttpServletRequest request, String name) {
        return intParameter(request, name, null);
    }

    protected Integer intParameter(HttpServletRequest request, String name, Integer defaultValue) {
        String value = parameter(request, name);
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    protected Integer requireIntParameter(HttpServletRequest request, String name) throws ValidationException {
        String value = parameter(request, name);
        if (value.isEmpty()) {
            throw new ValidationException("Required integer parameter '" + name + "' is missing.");
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Parameter '" + name + "' must be a valid integer number.");
        }
    }

    protected Boolean booleanParameter(HttpServletRequest request, String name) {
        return booleanParameter(request, name, false);
    }

    protected boolean booleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
        String value = parameter(request, name);
        if (value.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
    }

    // ==========================================
    // View Rendering & Redirection
    // ==========================================

    protected void render(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        if (viewPath == null || viewPath.isBlank() || viewPath.contains("..")) {
            throw new ServletException("Invalid view path: " + viewPath);
        }
        request.getRequestDispatcher("/WEB-INF/views/" + viewPath).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        String contextPath = request.getContextPath();
        String target = (contextPath == null || contextPath.isEmpty()) ? path : contextPath + path;
        String encoded = response.encodeRedirectURL(target);
        response.sendRedirect(encoded != null ? encoded : target);
    }

    // ==========================================
    // Central JSON Response Formatting
    // ==========================================

    protected void json(HttpServletResponse response, int status, Object payload) throws IOException {
        sendJson(response, status, payload);
    }

    protected void json(HttpServletResponse response, Object payload) throws IOException {
        sendJson(response, HttpServletResponse.SC_OK, payload);
    }

    protected void sendJson(HttpServletResponse response, int status, Object payload) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GSON.toJson(payload));
        response.getWriter().flush();
    }

    protected void sendJsonSuccess(HttpServletResponse response, Object data) throws IOException {
        sendJson(response, HttpServletResponse.SC_OK, JsonResponse.success(data));
    }

    protected void sendJsonSuccess(HttpServletResponse response, Object data, String message) throws IOException {
        sendJson(response, HttpServletResponse.SC_OK, JsonResponse.success(data, message));
    }

    protected void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        sendJson(response, status, JsonResponse.error(message));
    }

    protected void sendJsonError(HttpServletResponse response, String message) throws IOException {
        sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, message);
    }

    // ==========================================
    // Session Verification Helpers
    // ==========================================

    protected Object currentUser(HttpServletRequest request) {
        return currentUsername(request);
    }

    protected String currentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object username = session.getAttribute("username");
        return username instanceof String str ? str : null;
    }

    protected Long currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userId = session.getAttribute("userId");
        if (userId instanceof Long l) {
            return l;
        }
        if (userId instanceof Number num) {
            return num.longValue();
        }
        if (userId instanceof String str && !str.isBlank()) {
            try {
                return Long.valueOf(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    protected Role currentRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
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

    protected boolean isAuthenticated(HttpServletRequest request) {
        return currentUserId(request) != null && currentUsername(request) != null && currentRole(request) != null;
    }

    protected boolean hasRole(HttpServletRequest request, Role... allowedRoles) {
        Role userRole = currentRole(request);
        if (userRole == null || allowedRoles == null) {
            return false;
        }
        for (Role candidate : allowedRoles) {
            if (candidate == userRole) {
                return true;
            }
        }
        return false;
    }

    protected boolean requireAuthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isAuthenticated(request)) {
            if (isJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            } else {
                redirect(request, response, "/login");
            }
            return false;
        }
        return true;
    }

    protected boolean requireRole(HttpServletRequest request, HttpServletResponse response, Role... allowedRoles)
            throws IOException, ServletException {
        if (!requireAuthenticated(request, response)) {
            return false;
        }
        if (!hasRole(request, allowedRoles)) {
            if (isJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("errorMessage", "Access forbidden: insufficient role permissions");
                render(request, response, "error/403.jsp");
            }
            return false;
        }
        return true;
    }

    // ==========================================
    // Flash Messaging
    // ==========================================

    protected void setFlash(HttpServletRequest request, Flash flash) {
        Flash.set(request.getSession(true), flash);
    }

    protected void setFlashSuccess(HttpServletRequest request, String message) {
        setFlash(request, Flash.success(message));
    }

    protected void setFlashError(HttpServletRequest request, String message) {
        setFlash(request, Flash.error(message));
    }

    protected void setFlashInfo(HttpServletRequest request, String message) {
        setFlash(request, Flash.info(message));
    }

    protected void setFlashWarning(HttpServletRequest request, String message) {
        setFlash(request, Flash.warning(message));
    }

    protected Flash getFlash(HttpServletRequest request) {
        return Flash.consume(request.getSession(false));
    }

    // ==========================================
    // Central Exception & AJAX Detection
    // ==========================================

    protected void handleException(HttpServletRequest request, HttpServletResponse response, Exception ex)
            throws ServletException, IOException {
        int status = ErrorMapper.mapToStatus(ex);
        logger.error("Controller error encountered for request URI: {}", request.getRequestURI(), ex);
        if (isJsonRequest(request)) {
            sendJsonError(response, status, ex.getMessage() != null ? ex.getMessage() : "Internal server error");
            return;
        }
        response.setStatus(status);
        request.setAttribute("errorMessage", ex.getMessage());
        if (status == HttpServletResponse.SC_FORBIDDEN) {
            render(request, response, "error/403.jsp");
        } else if (status == HttpServletResponse.SC_NOT_FOUND) {
            render(request, response, "error/404.jsp");
        } else {
            render(request, response, "error/500.jsp");
        }
    }

    protected boolean isJsonRequest(HttpServletRequest request) {
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
