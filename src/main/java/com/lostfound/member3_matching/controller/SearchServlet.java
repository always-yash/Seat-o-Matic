package com.lostfound.member3_matching.controller;

import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member2_items.dao.ItemDAO;
import com.lostfound.member2_items.dao.ItemDAOImpl;
import com.lostfound.member3_matching.service.SearchService;
import com.lostfound.model.Category;
import com.lostfound.model.Item;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * Search Controller Servlet.
 * Handles single or multi-criteria filtering and queries.
 */
@WebServlet(name = "SearchServlet", urlPatterns = {"/search", "/api/search"})
public class SearchServlet extends HttpServlet {

    private final SearchService searchService = new SearchService();
    private final ItemDAO itemDAO = new ItemDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        Integer categoryId = ValidationUtil.parseInt(request.getParameter("categoryId"));
        String location = request.getParameter("location");
        String itemType = request.getParameter("type");
        if (itemType == null) {
            itemType = request.getParameter("itemType");
        }
        String dateStr = request.getParameter("date");
        String status = request.getParameter("status");

        Date itemDate = null;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                itemDate = Date.valueOf(dateStr.trim());
            } catch (Exception ignored) {
            }
        }

        try {
            List<Category> categories = itemDAO.getAllCategories();
            request.setAttribute("categories", categories);

            // Execute search
            List<Item> results = searchService.search(keyword, categoryId, location, itemType, itemDate, status);
            request.setAttribute("items", results);
            request.setAttribute("totalFound", results.size());

            // Echo back search parameters for UI filters
            request.setAttribute("keyword", keyword);
            request.setAttribute("selectedCategory", categoryId);
            request.setAttribute("selectedLocation", location);
            request.setAttribute("selectedType", itemType != null ? itemType.toUpperCase() : "ALL");
            request.setAttribute("selectedDate", dateStr);
            request.setAttribute("selectedStatus", status);

            request.getRequestDispatcher("/search/search.jsp").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("error", "Error processing search: " + e.getMessage());
            request.getRequestDispatcher("/search/search.jsp").forward(request, response);
        }
    }
}
