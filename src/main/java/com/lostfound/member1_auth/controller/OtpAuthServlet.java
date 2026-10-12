package com.lostfound.member1_auth.controller;

import com.google.gson.JsonObject;
import com.lostfound.member1_auth.service.AuthOtpService;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Email & OTP Passwordless Authentication.
 * Supports sending login OTPs, verifying codes, establishing sessions,
 * and seamless redirection to the main portal page.
 */
@WebServlet(name = "OtpAuthServlet", urlPatterns = {"/auth/otp", "/api/auth/otp"})
public class OtpAuthServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(OtpAuthServlet.class.getName());
    private final UserService userService = new UserServiceImpl();
    private final AdminDAO adminDAO = new AdminDAOImpl();
    private final AuthOtpService authOtpService = AuthOtpService.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String email = request.getParameter("email");
        boolean isAjax = "true".equalsIgnoreCase(request.getHeader("X-Requested-With")) ||
                (request.getContentType() != null && request.getContentType().contains("application/json")) ||
                "true".equalsIgnoreCase(request.getParameter("ajax"));

        if ("send".equalsIgnoreCase(action)) {
            handleSendOtp(request, response, email, isAjax);
        } else if ("verify".equalsIgnoreCase(action)) {
            handleVerifyOtp(request, response, email, isAjax);
        } else {
            if (isAjax) {
                sendJsonResponse(response, false, "Invalid action specified.", null);
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp?error=" +
                        URLEncoder.encode("Invalid OTP authentication action.", StandardCharsets.UTF_8));
            }
        }
    }

    private void handleSendOtp(HttpServletRequest request, HttpServletResponse response, String email, boolean isAjax)
            throws ServletException, IOException {
        try {
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Please enter a valid email address.");
            }

            String otpCode = userService.sendLoginOtp(email);

            if (isAjax) {
                JsonObject data = new JsonObject();
                data.addProperty("email", email.trim().toLowerCase());
                data.addProperty("devOtp", otpCode); // For effortless testing
                data.addProperty("expiresInMinutes", 5);
                sendJsonResponse(response, true, "A 6-digit login verification code has been dispatched to " + email, data);
            } else {
                request.setAttribute("otpSent", true);
                request.setAttribute("email", email);
                request.setAttribute("devOtp", otpCode);
                request.setAttribute("info", "Verification code sent to " + email + ". (Dev OTP: " + otpCode + ")");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send login OTP: " + e.getMessage(), e);
            if (isAjax) {
                sendJsonResponse(response, false, e.getMessage(), null);
            } else {
                request.setAttribute("error", e.getMessage());
                request.setAttribute("email", email);
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        }
    }

    private void handleVerifyOtp(HttpServletRequest request, HttpServletResponse response, String email, boolean isAjax)
            throws ServletException, IOException {
        String otp = request.getParameter("otp");
        try {
            User user = userService.loginWithOtp(email, otp);

            // Invalidate old session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // Create new session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("user", user);
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("authProvider", "EMAIL_OTP");
            session.setMaxInactiveInterval(30 * 60);

            // Audit log
            adminDAO.logAudit(user.getUserId(), "OTP_LOGIN: " + user.getEmail(), request.getRemoteAddr());

            session.setAttribute("flashSuccess", "Welcome back, " + user.getName() + "! Signed in via Email OTP.");

            String targetRedirect = request.getContextPath() +
                    ("ADMIN".equalsIgnoreCase(user.getRole()) ? "/admin/dashboard" : "/index.jsp");

            if (isAjax) {
                JsonObject data = new JsonObject();
                data.addProperty("redirectUrl", targetRedirect);
                data.addProperty("userName", user.getName());
                data.addProperty("userRole", user.getRole());
                sendJsonResponse(response, true, "Authentication successful! Redirecting...", data);
            } else {
                response.sendRedirect(targetRedirect);
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "OTP Login verification failed: " + e.getMessage(), e);
            if (isAjax) {
                sendJsonResponse(response, false, e.getMessage(), null);
            } else {
                request.setAttribute("otpSent", true);
                request.setAttribute("email", email);
                request.setAttribute("error", e.getMessage());
                request.getRequestDispatcher("/login.jsp").forward(request, response);
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
