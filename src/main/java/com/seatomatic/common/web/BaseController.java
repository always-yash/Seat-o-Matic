package com.seatomatic.common.web;

import com.google.gson.Gson;
import com.seatomatic.common.exception.SecurityException;
import com.seatomatic.common.security.Role;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

public abstract class BaseController extends HttpServlet {
    private static final Gson GSON = new Gson();

    protected String parameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    protected Long longParameter(HttpServletRequest request, String name) {
        String value = parameter(request, name);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    protected void render(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        if (viewPath == null || viewPath.isBlank() || viewPath.contains("..")) {
            throw new ServletException("Invalid view path");
        }
        request.getRequestDispatcher("/WEB-INF/views/" + viewPath).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + path));
    }

    protected void json(HttpServletResponse response, int status, Object payload) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GSON.toJson(payload));
    }

    protected Object currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : session.getAttribute("username");
    }

    protected void requireRole(HttpServletRequest request, Role... allowedRoles) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SecurityException("Authentication required.");
        }
        Object roleValue = session.getAttribute("userRole");
        if (!(roleValue instanceof String roleString) || roleString.isBlank()) {
            throw new SecurityException("Authentication required.");
        }
        final Role actualRole;
        try {
            actualRole = Role.valueOf(roleString.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new SecurityException("Invalid session role.");
        }
        for (Role role : allowedRoles) {
            if (role == actualRole) {
                return;
            }
        }
        throw new SecurityException("You are not authorized to access this resource.");
    }
}
