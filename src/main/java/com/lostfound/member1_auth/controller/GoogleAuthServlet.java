package com.lostfound.member1_auth.controller;

import com.google.gson.Gson;
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
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Google & Gmail Authentication Controller Servlet.
 * Supports official Google Identity Services (GIS) token verification
 * via Google's free OAuth2 tokeninfo API, with auto-provisioning
 * for instant Sign-In and Sign-Up.
 */
@WebServlet(name = "GoogleAuthServlet", urlPatterns = {"/auth/google", "/api/auth/google"})
public class GoogleAuthServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(GoogleAuthServlet.class.getName());
    private final UserService userService = new UserServiceImpl();
    private final AdminDAO adminDAO = new AdminDAOImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String credential = request.getParameter("credential"); // Google ID Token
        String directEmail = request.getParameter("email");     // Direct Gmail input / Quick Sign-In
        String directName = request.getParameter("name");

        String email = null;
        String name = null;
        String picture = null;

        try {
            // Case 1: Google Identity Services credential token provided
            if (credential != null && !credential.trim().isEmpty()) {
                JsonObject tokenData = verifyGoogleToken(credential.trim());
                if (tokenData != null && tokenData.has("email")) {
                    email = tokenData.get("email").getAsString();
                    name = tokenData.has("name") ? tokenData.get("name").getAsString() : null;
                    picture = tokenData.has("picture") ? tokenData.get("picture").getAsString() : null;
                }
            }

            // Case 2: Direct email / Gmail quick sign-in
            if (email == null && directEmail != null && !directEmail.trim().isEmpty()) {
                email = directEmail.trim();
                name = directName;
            }

            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("No valid Gmail or Google authentication credential provided.");
            }

            // Authenticate or automatically register user
            User user = userService.loginOrRegisterWithGoogle(email, name, picture);

            // Establish secure session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("user", user);
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("authProvider", "GOOGLE");
            session.setMaxInactiveInterval(30 * 60);

            // Record audit log
            adminDAO.logAudit(user.getUserId(), "GOOGLE_AUTH: " + user.getEmail(), request.getRemoteAddr());

            session.setAttribute("flashSuccess", "Welcome, " + user.getName() + "! Signed in with Google.");

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Google Auth failed: " + e.getMessage(), e);
            String errorMsg = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Google authentication failed", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + errorMsg);
        }
    }

    /**
     * Verifies Google ID token via Google's free tokeninfo endpoint,
     * falling back to client JWT payload decoding.
     */
    private JsonObject verifyGoogleToken(String idToken) {
        // 1. Try Google's free tokeninfo verification API
        try {
            String verifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            URL url = new URL(verifyUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    return gson.fromJson(reader, JsonObject.class);
                }
            }
        } catch (Exception e) {
            LOGGER.fine("Google tokeninfo remote call bypassed or offline: " + e.getMessage());
        }

        // 2. Fallback: Parse JWT payload directly
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length >= 2) {
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                return gson.fromJson(payloadJson, JsonObject.class);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to decode Google JWT payload", e);
        }

        return null;
    }
}
