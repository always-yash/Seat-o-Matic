package com.lostfound.member2_items.controller;

import com.google.gson.Gson;
import com.lostfound.member2_items.util.ImageUploadUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Dedicated Upload Controller Servlet.
 * Handles standalone file uploads (returns JSON relative path).
 */


@WebServlet(name = "UploadServlet", urlPatterns = {"/api/items/upload", "/upload"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 20 * 1024 * 1024
)
public class UploadServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> jsonResponse = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "User must be authenticated to upload files.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        try {
            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                filePart = request.getPart("image");
            }

            if (filePart == null || filePart.getSize() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("success", false);
                jsonResponse.put("message", "No file part provided in request.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            String uploadRealPath = getServletContext().getRealPath("/uploads/items");
            String savedPath = ImageUploadUtil.saveUploadedImage(filePart, uploadRealPath);

            jsonResponse.put("success", true);
            jsonResponse.put("filePath", savedPath);
            jsonResponse.put("message", "File uploaded successfully.");
            response.getWriter().write(gson.toJson(jsonResponse));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("success", false);
            jsonResponse.put("message", e.getMessage());
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }
}
