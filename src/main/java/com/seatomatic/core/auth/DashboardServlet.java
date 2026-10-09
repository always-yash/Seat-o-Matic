package com.seatomatic.core.auth;

import com.seatomatic.common.security.Role;
import com.seatomatic.common.web.BaseController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object currentUser = currentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Object role = req.getSession(false).getAttribute("role");
        req.setAttribute("userRole", role != null ? Role.valueOf(role.toString().toUpperCase()) : Role.ADMIN);
        req.setAttribute("username", currentUser);
        render(req, resp, "dashboard.jsp");
    }
}
