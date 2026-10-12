package com.lostfound.member1_auth.controller;

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
 * Logout Controller Servlet.
 * Terminates user sessions safely.
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout", "/api/logout"})
public class LogoutServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) {
                adminDAO.logAudit(userId, "LOGOUT", request.getRemoteAddr());
            }
            session.invalidate();
        }

        HttpSession freshSession = request.getSession(true);
        freshSession.setAttribute("flashInfo", "You have been logged out successfully.");
        response.sendRedirect(request.getContextPath() + "/login.jsp?info=You+have+been+logged+out+successfully.");
    }
}
