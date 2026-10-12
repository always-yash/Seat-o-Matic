package com.lostfound.member5_admin.service;

import com.lostfound.member1_auth.dao.UserDAO;
import com.lostfound.member1_auth.dao.UserDAOImpl;
import com.lostfound.member2_items.dao.ItemDAO;
import com.lostfound.member2_items.dao.ItemDAOImpl;
import com.lostfound.member5_admin.dao.AdminDAO;
import com.lostfound.member5_admin.dao.AdminDAOImpl;
import com.lostfound.member5_admin.util.PdfExporter;
import com.lostfound.model.AuditLog;
import com.lostfound.model.Item;
import com.lostfound.model.User;

import java.util.List;
import java.util.Map;

/**
 * Admin Service.
 * Manages administrative workflows, user moderation, analytics,
 * and report generation.
 */
public class AdminService {

    private final AdminDAO adminDAO = new AdminDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ItemDAO itemDAO = new ItemDAOImpl();

    public Map<String, Object> getDashboardStats() throws Exception {
        return adminDAO.getDashboardStatistics();
    }

    public Map<String, Object> getAnalytics() throws Exception {
        return adminDAO.getAnalyticsData();
    }

    public List<User> getAllUsers() throws Exception {
        return userDAO.findAll();
    }

    public List<User> searchUsers(String keyword) throws Exception {
        return userDAO.searchUsers(keyword);
    }

    public boolean blockUser(Long targetUserId, Long actingAdminId) throws Exception {
        if (targetUserId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }
        if (targetUserId.equals(actingAdminId)) {
            throw new IllegalArgumentException("Security Policy: An administrator cannot block their own account.");
        }

        User user = userDAO.findById(targetUserId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (user.isAdmin()) {
            int adminCount = userDAO.countAdmins();
            if (adminCount <= 1) {
                throw new IllegalStateException("Security Policy: Cannot block the last remaining Administrator account.");
            }
        }

        return userDAO.blockUser(targetUserId);
    }

    public boolean unblockUser(Long targetUserId) throws Exception {
        if (targetUserId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }
        return userDAO.unblockUser(targetUserId);
    }

    public boolean updateUserRole(Long targetUserId, String newRole, Long actingAdminId) throws Exception {
        if (targetUserId == null || newRole == null) {
            throw new IllegalArgumentException("Target user ID and new role are required.");
        }

        User user = userDAO.findById(targetUserId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        // Check if removing the last admin
        if (user.isAdmin() && !"ADMIN".equalsIgnoreCase(newRole)) {
            int adminCount = userDAO.countAdmins();
            if (adminCount <= 1) {
                throw new IllegalStateException("Security Policy: Cannot remove administrative privileges from the last active Administrator.");
            }
        }

        return userDAO.updateRole(targetUserId, newRole.toUpperCase());
    }

    public List<AuditLog> getRecentAuditLogs(int limit) throws Exception {
        return adminDAO.getRecentAuditLogs(limit);
    }

    public List<Item> getDisposalCandidates(int daysThreshold) throws Exception {
        return adminDAO.getDisposalCandidates(daysThreshold);
    }

    public byte[] exportSystemSummaryPdf() throws Exception {
        Map<String, Object> stats = adminDAO.getDashboardStatistics();
        List<Item> items = itemDAO.findAll();
        return PdfExporter.generateSummaryPdf(stats, items);
    }
}
