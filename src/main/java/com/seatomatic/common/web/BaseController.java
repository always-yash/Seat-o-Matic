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
import java.util.Objects;

public abstract class BaseController extends HttpServlet {

    private static final Gson GSON = new Gson();

    protected void render(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + viewPath).forward(request, response);
    }

    protected void redirect(HttpServletResponse response, String targetUrl) throws IOException {
        response.sendRedirect(response.encodeRedirectURL(targetUrl));
    }

    protected void json(HttpServletResponse response, Object payload) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GSON.toJson(payload));
    }

    protected Object currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return session.getAttribute("currentUser");
    }

    protected void requireRole(HttpServletRequest request, Role... roles) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SecurityException("Authentication required.");
        }

        Object userRole = session.getAttribute("role");
        if (userRole == null) {
            throw new SecurityException("Access denied.");
        }

        Role actualRole = Role.valueOf(userRole.toString().trim().toUpperCase());
        for (Role allowedRole : roles) {
            if (Objects.equals(allowedRole, actualRole)) {
                return;
            }
        }

        throw new SecurityException("You are not authorized to access this resource.");
    }
}
