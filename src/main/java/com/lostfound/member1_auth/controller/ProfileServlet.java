package com.lostfound.member1_auth.controller;

import com.lostfound.member1_auth.service.UserService;
import com.lostfound.member1_auth.service.UserServiceImpl;
import com.lostfound.member5_admin.dao.AdminDAO;
import com.lostfound.member5_admin.dao.AdminDAOImpl;
import com.lostfound.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * User Profile Controller Servlet.
 * Allows viewing and editing profile details.
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile", "/api/profile"})
public class ProfileServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();
    private final AdminDAO adminDAO = new AdminDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please+log+in+to+view+your+profile");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        try {
            User user = userService.getProfile(userId);
            request.setAttribute("profileUser", user);
            request.getRequestDispatcher("/auth/profile.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/index.jsp?error=" + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String department = request.getParameter("department");

        try {
            boolean updated = userService.updateProfile(userId, name, phone, department);
            if (updated) {
                // Update session state
                User updatedUser = userService.getProfile(userId);
                session.setAttribute("user", updatedUser);
                session.setAttribute("userName", updatedUser.getName());

                adminDAO.logAudit(userId, "UPDATE_PROFILE", request.getRemoteAddr());
                request.setAttribute("success", "Profile updated successfully.");
                request.setAttribute("profileUser", updatedUser);
            } else {
                request.setAttribute("error", "Failed to update profile.");
                request.setAttribute("profileUser", userService.getProfile(userId));
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            try {
                request.setAttribute("profileUser", userService.getProfile(userId));
            } catch (Exception ignored) {
            }
        }

        request.getRequestDispatcher("/auth/profile.jsp").forward(request, response);
    }
}
