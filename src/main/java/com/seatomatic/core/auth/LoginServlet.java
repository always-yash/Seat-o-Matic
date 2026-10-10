package com.seatomatic.core.auth;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.security.CsrfTokenManager;
import com.seatomatic.common.web.BaseController;
import com.seatomatic.report.audit.AuditPublisherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);
    private final AuthService authService = new AuthService();
    private final AuditPublisher auditPublisher = new AuditPublisherImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        String token = CsrfTokenManager.token(session);
        if (token == null) {
            token = CsrfTokenManager.generateToken(session);
        }
        request.setAttribute("csrfToken", token);
        render(request, response, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = parameter(request, "username");
        String password = request.getParameter("password");
        try {
            User user = authService.authenticate(username, password);
            if (user == null) {
                auditPublisher.publish("LOGIN_FAILURE", username, "USER", null, "{\"reason\":\"invalid_credentials\"}");
                showLoginError(request, response, "Invalid username or password.");
                return;
            }

            request.changeSessionId();
            HttpSession session = request.getSession(false);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userRole", user.getRole().name());
            session.setAttribute("role", user.getRole().name());
            session.setAttribute("currentUser", user.getUsername());
            session.setAttribute("fullName", user.getFullName());
            CsrfTokenManager.generateToken(session);
            auditPublisher.publish("LOGIN_SUCCESS", user.getUsername(), "USER", user.getId(), "{\"method\":\"password\"}");
            redirect(request, response, "/dashboard");
        } catch (SQLException ex) {
            LOGGER.error("Authentication lookup failed for username {}", username, ex);
            showLoginError(request, response, "Sign-in is temporarily unavailable. Please try again.");
        }
    }

    private void showLoginError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.setAttribute("csrfToken", CsrfTokenManager.generateToken(request.getSession(true)));
        render(request, response, "login.jsp");
    }
}
