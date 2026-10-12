package com.lostfound.member1_auth.controller;

import com.lostfound.member1_auth.service.UserService;
import com.lostfound.member1_auth.service.UserServiceImpl;
import com.lostfound.member5_admin.dao.AdminDAO;
import com.lostfound.member5_admin.dao.AdminDAOImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Change Password Controller Servlet.
 * Enables secure credential updates.
 */
@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/change-password", "/api/change-password"})
public class ChangePasswordServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();
    private final AdminDAO adminDAO = new AdminDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please+log+in+first");
            return;
        }
        request.getRequestDispatcher("/auth/change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            boolean success = userService.changePassword(userId, currentPassword, newPassword, confirmPassword);
            if (success) {
                adminDAO.logAudit(userId, "CHANGE_PASSWORD", request.getRemoteAddr());
                request.setAttribute("success", "Password updated successfully. Please keep your new password safe.");
            } else {
                request.setAttribute("error", "Unable to update password. Please verify current password.");
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
        }

        request.getRequestDispatcher("/auth/change-password.jsp").forward(request, response);
    }
}
