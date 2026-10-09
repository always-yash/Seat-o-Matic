package com.seatomatic.core.auth;

import com.seatomatic.common.security.CsrfTokenManager;
import com.seatomatic.common.web.BaseController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends BaseController {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getSession(true);
        CsrfTokenManager.generateToken(req.getSession());
        req.setAttribute("csrfToken", CsrfTokenManager.token(req.getSession()));
        render(req, resp, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        User user = authService.authenticate(username, password);

        if (user == null) {
            req.setAttribute("errorMessage", "Invalid username or password.");
            req.setAttribute("csrfToken", CsrfTokenManager.generateToken(req.getSession(true)));
            render(req, resp, "login.jsp");
            return;
        }

        req.changeSessionId();
        req.getSession(false).setAttribute("currentUser", user.getUsername());
        req.getSession(false).setAttribute("role", user.getRole().name());
        req.getSession(false).setAttribute("userId", user.getId());
        req.getSession(false).setAttribute("fullName", user.getFullName());

        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
