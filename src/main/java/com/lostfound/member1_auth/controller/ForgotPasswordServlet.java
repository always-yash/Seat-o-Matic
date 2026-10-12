package com.lostfound.member1_auth.controller;

import com.google.gson.JsonObject;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Password Recovery / Forgot Password workflow.
 * Handles dispatching 6-digit recovery OTP codes and resetting credentials.
 */
@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/forgot-password", "/api/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordServlet.class.getName());
    private final UserService userService = new UserServiceImpl();
    private final AdminDAO adminDAO = new AdminDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String email = request.getParameter("email");
        boolean isAjax = "true".equalsIgnoreCase(request.getHeader("X-Requested-With")) ||
                (request.getContentType() != null && request.getContentType().contains("application/json")) ||
                "true".equalsIgnoreCase(request.getParameter("ajax"));

        if ("send-otp".equalsIgnoreCase(action)) {
            handleSendRecoveryOtp(request, response, email, isAjax);
        } else if ("reset".equalsIgnoreCase(action)) {
            handleResetPassword(request, response, email, isAjax);
        } else {
            if (isAjax) {
                sendJsonResponse(response, false, "Unknown password reset action.", null);
            } else {
                response.sendRedirect(request.getContextPath() + "/forgot-password.jsp");
            }
        }
    }

    private void handleSendRecoveryOtp(HttpServletRequest request, HttpServletResponse response, String email, boolean isAjax)
            throws ServletException, IOException {
        try {
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Please enter your registered email address.");
            }

            String otpCode = userService.sendForgotPasswordOtp(email);

            if (isAjax) {
                JsonObject data = new JsonObject();
                data.addProperty("email", email.trim().toLowerCase());
                data.addProperty("devOtp", otpCode);
                data.addProperty("expiresInMinutes", 10);
                sendJsonResponse(response, true, "A 6-digit password recovery code has been sent to " + email, data);
            } else {
                request.setAttribute("step", "verify");
                request.setAttribute("email", email);
                request.setAttribute("devOtp", otpCode);
                request.setAttribute("info", "Recovery code sent to " + email + ". (Dev Code: " + otpCode + ")");
                request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send reset OTP: " + e.getMessage(), e);
            if (isAjax) {
                sendJsonResponse(response, false, e.getMessage(), null);
            } else {
                request.setAttribute("error", e.getMessage());
                request.setAttribute("email", email);
                request.setAttribute("step", "request");
                request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
            }
        }
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response, String email, boolean isAjax)
            throws ServletException, IOException {
        String otp = request.getParameter("otp");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            boolean success = userService.resetPasswordWithOtp(email, otp, newPassword, confirmPassword);

            if (success) {
                adminDAO.logAudit(null, "PASSWORD_RESET_SUCCESS: " + email, request.getRemoteAddr());

                request.getSession().setAttribute("flashSuccess",
                        "Your password has been successfully reset! Please sign in with your new credentials.");

                if (isAjax) {
                    JsonObject data = new JsonObject();
                    data.addProperty("redirectUrl", request.getContextPath() + "/login.jsp");
                    sendJsonResponse(response, true, "Password updated successfully!", data);
                } else {
                    response.sendRedirect(request.getContextPath() + "/login.jsp");
                }
            } else {
                throw new RuntimeException("Could not update password. Please try again.");
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Password reset failed: " + e.getMessage(), e);
            if (isAjax) {
                sendJsonResponse(response, false, e.getMessage(), null);
            } else {
                request.setAttribute("error", e.getMessage());
                request.setAttribute("email", email);
                request.setAttribute("step", "verify");
                request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
            }
        }
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message, JsonObject data)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject root = new JsonObject();
        root.addProperty("success", success);
        root.addProperty("message", message);
        if (data != null) {
            root.add("data", data);
        }
        response.getWriter().write(root.toString());
    }
}
