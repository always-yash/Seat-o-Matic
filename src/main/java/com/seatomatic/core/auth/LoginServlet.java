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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);
    private static final String DEBUG_ERRORS_PROPERTY = "seatomatic.auth.debugErrors";
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

            HttpSession session = request.getSession(false);
            if (session == null) {
                session = request.getSession(true);
            } else {
                request.changeSessionId();
            }
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
            handleAuthenticationException(request, response, ex);
        } catch (RuntimeException ex) {
            LOGGER.error("Unexpected authentication failure for username {}", username, ex);
            handleAuthenticationException(request, response, ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected checked authentication failure for username {}", username, ex);
            handleAuthenticationException(request, response, ex);
        }
    }

    private void handleAuthenticationException(HttpServletRequest request, HttpServletResponse response,
                                               Exception exception)
            throws ServletException, IOException {
        if (Boolean.getBoolean(DEBUG_ERRORS_PROPERTY)) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            StringWriter stackTrace = new StringWriter();
            exception.printStackTrace(new java.io.PrintWriter(stackTrace));
            out.println("<h3 style='color:red;'>CRASH ERROR: "
                    + escapeHtml(exception.getMessage()) + "</h3>");
            out.println("<pre>" + escapeHtml(stackTrace.toString()) + "</pre>");
            return;
        }
        showLoginError(request, response, "Sign-in is temporarily unavailable. Please try again.");
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "(no exception message)";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void showLoginError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.setAttribute("csrfToken", CsrfTokenManager.generateToken(request.getSession(true)));
        render(request, response, "login.jsp");
    }
}
