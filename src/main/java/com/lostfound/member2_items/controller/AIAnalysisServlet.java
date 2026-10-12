package com.lostfound.member2_items.controller;

import com.google.gson.Gson;
import com.lostfound.member2_items.service.AIAnalysisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/ai-analysis")
public class AIAnalysisServlet extends HttpServlet {

    private final AIAnalysisService aiService = new AIAnalysisService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> result = new HashMap<>();

        try {
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String location = request.getParameter("location");

            if (title == null || description == null || location == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Title, description and location are required.");
            } else {
                String analysis =
                        aiService.analyze(title, description, location);

                result.put("analysis", analysis);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "AI analysis failed.");
        }

        response.getWriter().write(gson.toJson(result));
    }
}