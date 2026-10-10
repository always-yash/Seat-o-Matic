package com.seatomatic.core.auth;

import com.seatomatic.common.security.Role;
import com.seatomatic.common.filter.AuthorizationFilter;
import com.seatomatic.common.web.BaseController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object username = currentUser(request);
        if (username == null) {
            redirect(request, response, "/login");
            return;
        }
        Object roleValue = request.getSession(false).getAttribute("userRole");
        Role role = AuthorizationFilter.parseRole(roleValue);
        if (role == null) {
            request.getSession(false).invalidate();
            redirect(request, response, "/login");
            return;
        }
        request.setAttribute("username", username);
        request.setAttribute("userRole", role);
        render(request, response, "dashboard.jsp");
    }
}
