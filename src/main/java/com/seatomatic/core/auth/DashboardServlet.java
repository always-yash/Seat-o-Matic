package com.seatomatic.core.auth;

import com.seatomatic.common.web.BaseController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends BaseController {
    private final DashboardStatsDao dashboardStatsDao;

    public DashboardServlet() {
        this(new DashboardStatsDao());
    }

    DashboardServlet(DashboardStatsDao dashboardStatsDao) {
        this.dashboardStatsDao = dashboardStatsDao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object username = currentUser(request);
        if (username == null) {
            redirect(request, response, "/login");
            return;
        }
        request.setAttribute("username", username);
        request.setAttribute("userRole", request.getSession(false).getAttribute("userRole"));
        try {
            request.setAttribute("dashboardStats", dashboardStatsDao.load());
            render(request, response, "dashboard.jsp");
        } catch (SQLException ex) {
            throw new ServletException("Unable to load dashboard statistics", ex);
        }
    }
}
