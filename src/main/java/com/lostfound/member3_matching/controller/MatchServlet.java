package com.lostfound.member3_matching.controller;

import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member2_items.service.ItemService;
import com.lostfound.member2_items.service.ItemServiceImpl;
import com.lostfound.member3_matching.service.MatchService;
import com.lostfound.model.Item;
import com.lostfound.model.MatchResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;



/**
 * Match Controller Servlet.
 * Computes and displays algorithmic match scores for a selected item.
 */
@WebServlet(name = "MatchServlet", urlPatterns = {"/matches", "/api/matches"})
public class MatchServlet extends HttpServlet {

    private final MatchService matchService = new MatchService();
    private final ItemService itemService = new ItemServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String itemIdStr = request.getParameter("itemId");
        if (itemIdStr == null) {
            itemIdStr = request.getParameter("id");
        }

        Long itemId = ValidationUtil.parseLong(itemIdStr);
        if (itemId == null) {
            response.sendRedirect(request.getContextPath() + "/items?action=gallery&error=Item+ID+required+for+matching");
            return;
        }

        try {
            Item targetItem = itemService.getItemById(itemId);
            if (targetItem == null) {
                response.sendRedirect(request.getContextPath() + "/items?action=gallery&error=Item+not+found");
                return;
            }
            List<MatchResult> matches = matchService.findMatchesForItem(itemId);

            request.setAttribute("targetItem", targetItem);
            request.setAttribute("matches", matches);
            request.setAttribute("matchCount", matches.size());

            request.getRequestDispatcher("/search/match-results.jsp").forward(request, response);

        } catch (Exception e) {
            String msg = java.net.URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Error finding matches", java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/items?action=view&id=" + itemId + "&error=" + msg);
        }
    }
}
