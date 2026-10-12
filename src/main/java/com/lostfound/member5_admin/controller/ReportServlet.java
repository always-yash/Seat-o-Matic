package com.lostfound.member5_admin.controller;

import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member5_admin.dao.AdminDAO;
import com.lostfound.member5_admin.dao.AdminDAOImpl;
import com.lostfound.member5_admin.service.ReportService;
import com.lostfound.model.Report;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Report Controller Servlet.
 * Handles flagging inappropriate items and administrative moderation.
 */
@WebServlet(name = "ReportServlet", urlPatterns = {"/reports", "/api/reports", "/admin/reports"})
public class ReportServlet extends HttpServlet {

    private final ReportService reportService = new ReportService();
    private final AdminDAO adminDAO = new AdminDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please+log+in+first");
            return;
        }

        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only administrators can view item moderation reports.");
            return;
        }

        try {
            List<Report> reports = reportService.getAllReports();
            request.setAttribute("reports", reports);
            request.getRequestDispatcher("/admin/reports.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/admin/reports.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please+log+in+first");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");
        String action = request.getParameter("action");

        try {
            if ("file".equalsIgnoreCase(action)) {
                Long itemId = ValidationUtil.parseLong(request.getParameter("itemId"));
                String reason = request.getParameter("reason");
                String description = request.getParameter("description");

                reportService.fileReport(userId, itemId, reason, description);
                adminDAO.logAudit(userId, "FILE_REPORT: Item #" + itemId + " (" + reason + ")", request.getRemoteAddr());

                request.getSession().setAttribute("flashSuccess", "Thank you. Your report has been submitted to campus administrators for review.");
                if (itemId != null) {
                    response.sendRedirect(request.getContextPath() + "/items?action=view&id=" + itemId);
                } else {
                    response.sendRedirect(request.getContextPath() + "/index.jsp");
                }

            } else if ("updateStatus".equalsIgnoreCase(action)) {
                if (!"ADMIN".equalsIgnoreCase(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access required.");
                    return;
                }

                Long reportId = ValidationUtil.parseLong(request.getParameter("reportId"));
                String newStatus = request.getParameter("status");

                reportService.updateReportStatus(reportId, newStatus);
                adminDAO.logAudit(userId, "UPDATE_REPORT_STATUS: #" + reportId + " to " + newStatus, request.getRemoteAddr());

                request.getSession().setAttribute("flashSuccess", "Report #" + reportId + " status updated to " + newStatus + ".");
                response.sendRedirect(request.getContextPath() + "/admin/reports");
            } else {
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            }
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        }
    }
}
