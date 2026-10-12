package com.lostfound.member5_admin.controller;

import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member5_admin.dao.AdminDAO;
import com.lostfound.member5_admin.dao.AdminDAOImpl;
import com.lostfound.member5_admin.service.AdminService;
import com.lostfound.model.AuditLog;
import com.lostfound.model.Item;
import com.lostfound.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Admin Controller Servlet.
 * Central hub for administration, user moderation, analytics,
 * disposal review, and PDF report downloads.
 */
@WebServlet(name = "AdminServlet", urlPatterns = {
        "/admin/dashboard",
        "/admin/users",
        "/admin/analytics",
        "/admin/disposal",
        "/admin/export-pdf",
        "/api/admin/dashboard",
        "/api/admin/users",
        "/api/admin/users/*"
})
public class AdminServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();
    private final AdminDAO adminDAO = new AdminDAOImpl();

    private boolean verifyAdminAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please+log+in+as+administrator");
            return false;
        }

        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Administrator privileges required.");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!verifyAdminAccess(request, response)) return;

        String path = request.getServletPath();
        try {
            if (path.endsWith("/users")) {
                handleUsers(request, response);
            } else if (path.endsWith("/analytics")) {
                handleAnalytics(request, response);
            } else if (path.endsWith("/disposal")) {
                handleDisposal(request, response);
            } else if (path.endsWith("/export-pdf")) {
                handlePdfExport(request, response);
            } else {
                handleDashboard(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!verifyAdminAccess(request, response)) return;

        HttpSession session = request.getSession();
        Long adminId = (Long) session.getAttribute("userId");
        String action = request.getParameter("action");

        try {
            if ("block".equalsIgnoreCase(action)) {
                Long targetId = ValidationUtil.parseLong(request.getParameter("userId"));
                adminService.blockUser(targetId, adminId);
                adminDAO.logAudit(adminId, "BLOCK_USER: User #" + targetId, request.getRemoteAddr());
                request.getSession().setAttribute("flashSuccess", "User #" + targetId + " has been blocked.");
            } else if ("unblock".equalsIgnoreCase(action)) {
                Long targetId = ValidationUtil.parseLong(request.getParameter("userId"));
                adminService.unblockUser(targetId);
                adminDAO.logAudit(adminId, "UNBLOCK_USER: User #" + targetId, request.getRemoteAddr());
                request.getSession().setAttribute("flashSuccess", "User #" + targetId + " has been unblocked.");
            } else if ("updateRole".equalsIgnoreCase(action)) {
                Long targetId = ValidationUtil.parseLong(request.getParameter("userId"));
                String newRole = request.getParameter("newRole");
                adminService.updateUserRole(targetId, newRole, adminId);
                adminDAO.logAudit(adminId, "UPDATE_USER_ROLE: User #" + targetId + " to " + newRole, request.getRemoteAddr());
                request.getSession().setAttribute("flashSuccess", "User role updated successfully.");
            }
            response.sendRedirect(request.getContextPath() + "/admin/users");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private void handleDashboard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> stats = adminService.getDashboardStats();
        List<AuditLog> auditLogs = adminService.getRecentAuditLogs(15);

        request.setAttribute("stats", stats);
        request.setAttribute("auditLogs", auditLogs);
        request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
    }

    private void handleUsers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String keyword = request.getParameter("keyword");
        List<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = adminService.searchUsers(keyword);
            request.setAttribute("keyword", keyword);
        } else {
            users = adminService.getAllUsers();
        }

        request.setAttribute("users", users);
        request.getRequestDispatcher("/admin/users.jsp").forward(request, response);
    }

    private void handleAnalytics(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> stats = adminService.getDashboardStats();
        Map<String, Object> analytics = adminService.getAnalytics();

        request.setAttribute("stats", stats);
        request.setAttribute("analytics", analytics);
        request.getRequestDispatcher("/admin/analytics.jsp").forward(request, response);
    }

    private void handleDisposal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int threshold = 60; // 60 days
        List<Item> candidates = adminService.getDisposalCandidates(threshold);
        request.setAttribute("candidates", candidates);
        request.setAttribute("threshold", threshold);
        request.getRequestDispatcher("/admin/disposal.jsp").forward(request, response);
    }

    private void handlePdfExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        byte[] pdfBytes = adminService.exportSystemSummaryPdf();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"LostFound_Campus_Report.pdf\"");
        response.setContentLength(pdfBytes.length);

        try (OutputStream os = response.getOutputStream()) {
            os.write(pdfBytes);
            os.flush();
        }
    }
}
