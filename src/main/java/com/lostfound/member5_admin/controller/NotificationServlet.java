package com.lostfound.member5_admin.controller;

import com.google.gson.Gson;
import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member5_admin.service.NotificationService;
import com.lostfound.model.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notification Controller Servlet.
 * Provides notification listings, unread counters, and read-status management.
 */
@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications", "/api/notifications"})
public class NotificationServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please+log+in+first");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        String format = request.getParameter("format");

        try {
            List<Notification> notifications = notificationService.getNotificationsForUser(userId);
            int unreadCount = notificationService.countUnreadNotifications(userId);

            if ("json".equalsIgnoreCase(format)) {
                response.setContentType("application/json");
                Map<String, Object> data = new HashMap<>();
                data.put("unreadCount", unreadCount);
                data.put("notifications", notifications);
                response.getWriter().write(gson.toJson(data));
                return;
            }

            request.setAttribute("notifications", notifications);
            request.setAttribute("unreadCount", unreadCount);
            request.getRequestDispatcher("/admin/notifications.jsp").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/admin/notifications.jsp").forward(request, response);
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
        String action = request.getParameter("action");

        try {
            if ("mark-all-read".equalsIgnoreCase(action)) {
                notificationService.markAllAsRead(userId);
                request.getSession().setAttribute("flashSuccess", "All notifications marked as read.");
            } else if ("mark-read".equalsIgnoreCase(action)) {
                Long notifId = ValidationUtil.parseLong(request.getParameter("id"));
                if (notifId != null) {
                    notificationService.markAsRead(notifId, userId);
                }
            }
            response.sendRedirect(request.getContextPath() + "/notifications");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/notifications");
        }
    }
}
