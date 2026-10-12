package com.lostfound.member2_items.controller;

import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member2_items.service.ItemService;
import com.lostfound.member2_items.service.ItemServiceImpl;
import com.lostfound.member2_items.util.ImageUploadUtil;
import com.lostfound.member5_admin.dao.AdminDAO;
import com.lostfound.member5_admin.dao.AdminDAOImpl;
import com.lostfound.model.Category;
import com.lostfound.model.Item;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * Item Controller Servlet.
 * Handles reporting lost/found items, editing, viewing, and listing.
 */
@WebServlet(name = "ItemServlet", urlPatterns = {"/items", "/api/items"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,       // 1 MB
        maxFileSize = 5 * 1024 * 1024,          // 5 MB
        maxRequestSize = 20 * 1024 * 1024       // 20 MB
)
public class ItemServlet extends HttpServlet {

    private final ItemService itemService = new ItemServiceImpl();
    private final AdminDAO adminDAO = new AdminDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "gallery";
        }

        try {
            switch (action) {
                case "view":
                    handleViewItem(request, response);
                    break;
                case "report-lost":
                    ensureLoggedIn(request, response, () -> forwardToReportPage(request, response, "LOST"));
                    break;
                case "report-found":
                    ensureLoggedIn(request, response, () -> forwardToReportPage(request, response, "FOUND"));
                    break;
                case "my-items":
                    ensureLoggedIn(request, response, () -> handleMyItems(request, response));
                    break;
                case "edit":
                    ensureLoggedIn(request, response, () -> handleEditPage(request, response));
                    break;
                case "gallery":
                default:
                    handleGallery(request, response);
                    break;

                    
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/items/gallery.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please+log+in+to+perform+this+action");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        try {
            if ("create".equalsIgnoreCase(action)) {
                handleCreateItem(request, response, userId);
            } else if ("update".equalsIgnoreCase(action)) {
                handleUpdateItem(request, response, userId, role);
            } else if ("delete".equalsIgnoreCase(action)) {
                handleDeleteItem(request, response, userId, role);
            } else {
                response.sendRedirect(request.getContextPath() + "/items?action=gallery");
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            try {
                request.setAttribute("categories", itemService.getAllCategories());
            } catch (Exception ignored) {}
            String formType = request.getParameter("itemType");
            if ("FOUND".equalsIgnoreCase(formType)) {
                request.getRequestDispatcher("/items/report-found.jsp").forward(request, response);
            } else {
                request.getRequestDispatcher("/items/report-lost.jsp").forward(request, response);
            }
        }
    }

    private void handleViewItem(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long id = ValidationUtil.parseLong(request.getParameter("id"));
        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/items?action=gallery");
            return;
        }
        Item item = itemService.getItemById(id);
        request.setAttribute("item", item);
        try {
            String noticeUrl = request.getRequestURL().toString() + "?action=view&id=" + item.getItemId();
            String qrBase64 = com.lostfound.member4_claims.util.QRCodeUtil.generateQRCodeBase64(noticeUrl, 220, 220);
            request.setAttribute("qrBase64", qrBase64);
        } catch (Exception ignored) {
        }
        request.getRequestDispatcher("/items/item-details.jsp").forward(request, response);
    }

    private void forwardToReportPage(HttpServletRequest request, HttpServletResponse response, String itemType) throws Exception {
        List<Category> categories = itemService.getAllCategories();
        request.setAttribute("categories", categories);
        request.setAttribute("itemType", itemType);
        if ("FOUND".equalsIgnoreCase(itemType)) {
            request.getRequestDispatcher("/items/report-found.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/items/report-lost.jsp").forward(request, response);
        }
    }

    private void handleMyItems(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        List<Item> myItems = itemService.getItemsByUser(userId);
        request.setAttribute("items", myItems);
        request.getRequestDispatcher("/items/my-items.jsp").forward(request, response);
    }

    private void handleEditPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long id = ValidationUtil.parseLong(request.getParameter("id"));
        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/items?action=my-items");
            return;
        }

        Item item = itemService.getItemById(id);
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        // Authorization check: Only owner or ADMIN can open edit page
        if (!"ADMIN".equalsIgnoreCase(role) && !item.getUserId().equals(userId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot edit an item reported by someone else.");
            return;
        }

        request.setAttribute("item", item);
        request.setAttribute("categories", itemService.getAllCategories());
        request.getRequestDispatcher("/items/edit-item.jsp").forward(request, response);
    }

    private void handleGallery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String type = request.getParameter("type");
        List<Item> items;
        if ("LOST".equalsIgnoreCase(type) || "FOUND".equalsIgnoreCase(type)) {
            items = itemService.getItemsByType(type.toUpperCase());
        } else {
            items = itemService.getAllItems();
        }
        request.setAttribute("items", items);
        request.setAttribute("currentType", type != null ? type.toUpperCase() : "ALL");
        request.setAttribute("categories", itemService.getAllCategories());
        request.getRequestDispatcher("/items/gallery.jsp").forward(request, response);
    }

    private void handleCreateItem(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        String itemType = request.getParameter("itemType");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        Integer categoryId = ValidationUtil.parseInt(request.getParameter("categoryId"));
        String location = request.getParameter("location");
        Double latitude = ValidationUtil.parseDouble(request.getParameter("latitude"));
        Double longitude = ValidationUtil.parseDouble(request.getParameter("longitude"));
        String dateStr = request.getParameter("itemDate");

        Date itemDate = (dateStr != null && !dateStr.isEmpty()) ? Date.valueOf(dateStr) : new Date(System.currentTimeMillis());

        // Handle Image Upload
        String imagePath = null;
        try {
            Part filePart = request.getPart("imageFile");
            if (filePart != null && filePart.getSize() > 0) {
                String uploadRealPath = getServletContext().getRealPath("/uploads/items");
                imagePath = ImageUploadUtil.saveUploadedImage(filePart, uploadRealPath);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Image upload failed: " + e.getMessage());
        }

        Item item = new Item();
        item.setUserId(userId);
        item.setCategoryId(categoryId);
        item.setTitle(title);
        item.setDescription(description);
        item.setItemType(itemType != null ? itemType.toUpperCase() : "LOST");
        item.setLocation(location);
        item.setLatitude(latitude != null ? latitude : 0.0);
        item.setLongitude(longitude != null ? longitude : 0.0);
        item.setItemDate(itemDate);
        item.setImage(imagePath);
        item.setStatus("ACTIVE");

        Item createdItem;
        if ("FOUND".equalsIgnoreCase(itemType)) {
            createdItem = itemService.reportFound(item);
        } else {
            createdItem = itemService.reportLost(item);
        }

        adminDAO.logAudit(userId, "CREATE_ITEM: " + createdItem.getTitle() + " (" + createdItem.getItemType() + ")", request.getRemoteAddr());

        request.getSession().setAttribute("flashSuccess", "Item reported successfully!");
        response.sendRedirect(request.getContextPath() + "/items?action=view&id=" + createdItem.getItemId());
    }

    private void handleUpdateItem(HttpServletRequest request, HttpServletResponse response, Long userId, String role) throws Exception {
        Long itemId = ValidationUtil.parseLong(request.getParameter("itemId"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        Integer categoryId = ValidationUtil.parseInt(request.getParameter("categoryId"));
        String location = request.getParameter("location");
        Double latitude = ValidationUtil.parseDouble(request.getParameter("latitude"));
        Double longitude = ValidationUtil.parseDouble(request.getParameter("longitude"));
        String dateStr = request.getParameter("itemDate");
        String status = request.getParameter("status");
        String existingImage = request.getParameter("existingImage");

        Date itemDate = (dateStr != null && !dateStr.isEmpty()) ? Date.valueOf(dateStr) : new Date(System.currentTimeMillis());

        String imagePath = existingImage;
        Part filePart = request.getPart("imageFile");
        if (filePart != null && filePart.getSize() > 0) {
            String uploadRealPath = getServletContext().getRealPath("/uploads/items");
            String newImagePath = ImageUploadUtil.saveUploadedImage(filePart, uploadRealPath);
            if (newImagePath != null) {
                imagePath = newImagePath;
            }
        }

        Item item = new Item();
        item.setItemId(itemId);
        item.setUserId(userId);
        item.setCategoryId(categoryId);
        item.setTitle(title);
        item.setDescription(description);
        item.setLocation(location);
        item.setLatitude(latitude != null ? latitude : 0.0);
        item.setLongitude(longitude != null ? longitude : 0.0);
        item.setItemDate(itemDate);
        item.setImage(imagePath);
        item.setStatus(status != null ? status : "ACTIVE");

        itemService.updateItem(item, userId, role);
        adminDAO.logAudit(userId, "UPDATE_ITEM: #" + itemId + " (" + item.getTitle() + ")", request.getRemoteAddr());

        request.getSession().setAttribute("flashSuccess", "Item updated successfully!");
        response.sendRedirect(request.getContextPath() + "/items?action=view&id=" + itemId);
    }

    private void handleDeleteItem(HttpServletRequest request, HttpServletResponse response, Long userId, String role) throws Exception {
        Long itemId = ValidationUtil.parseLong(request.getParameter("itemId"));
        itemService.deleteItem(itemId, userId, role);
        adminDAO.logAudit(userId, "DELETE_ITEM: #" + itemId, request.getRemoteAddr());

        request.getSession().setAttribute("flashSuccess", "Item removed successfully.");
        response.sendRedirect(request.getContextPath() + "/items?action=my-items");
    }

    private void ensureLoggedIn(HttpServletRequest request, HttpServletResponse response, RunnableWithException action) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please+log+in+first");
            return;
        }
        action.run();
    }

    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }
}
