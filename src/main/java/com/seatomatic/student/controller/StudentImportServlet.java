package com.seatomatic.student.controller;

import com.seatomatic.common.security.Role;
import com.seatomatic.common.web.BaseController;
import com.seatomatic.student.model.dto.BatchImportResult;
import com.seatomatic.student.service.StudentImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

/**
 * Controller handling multipart CSV bulk student registration uploads.
 */
@WebServlet(name = "StudentImportServlet", urlPatterns = "/students/import")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 10 * 1024 * 1024,       // 10 MB
        maxRequestSize = 15 * 1024 * 1024     // 15 MB
)
public class StudentImportServlet extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentImportServlet.class);

    private final StudentImportService importService;

    public StudentImportServlet() {
        this(new StudentImportService());
    }

    public StudentImportServlet(StudentImportService importService) {
        this.importService = importService != null ? importService : new StudentImportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        redirect(request, response, "/students");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireRole(request, response, Role.ADMIN, Role.FACULTY)) {
            return;
        }

        try {
            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                setFlashError(request, "Please choose a valid CSV file to upload.");
                redirect(request, response, "/students");
                return;
            }

            String submittedFileName = filePart.getSubmittedFileName();
            if (submittedFileName != null && !submittedFileName.toLowerCase().endsWith(".csv")) {
                setFlashError(request, "Invalid file format: only .csv files are supported.");
                redirect(request, response, "/students");
                return;
            }

            String actor = currentUsername(request);
            BatchImportResult result;
            try (InputStream is = filePart.getInputStream()) {
                result = importService.importFromCsv(is, actor);
            }

            HttpSession session = request.getSession(true);
            if (result.hasErrors()) {
                session.setAttribute("lastImportErrors", result.getErrors());
            } else {
                session.removeAttribute("lastImportErrors");
            }

            if (result.getImportedCount() > 0) {
                StringBuilder msg = new StringBuilder();
                msg.append("Bulk Import Complete: Registered ").append(result.getImportedCount()).append(" students.");
                if (result.getSkippedCount() > 0) {
                    msg.append(" (").append(result.getSkippedCount()).append(" skipped duplicate records)");
                }
                if (result.getErrorCount() > 0) {
                    msg.append(" [").append(result.getErrorCount()).append(" validation issues encountered]");
                }
                setFlashSuccess(request, msg.toString());
            } else if (result.getSkippedCount() > 0 && result.getErrorCount() == 0) {
                setFlashWarning(request, "No new students imported: all " + result.getSkippedCount() + " records already exist in database.");
            } else {
                setFlashError(request, "CSV import encountered " + result.getErrorCount() + " validation errors. No records were imported.");
            }

            redirect(request, response, "/students");
        } catch (Exception ex) {
            LOGGER.error("Failed to process bulk student CSV upload", ex);
            setFlashError(request, "Failed to process CSV upload: " + ex.getMessage());
            redirect(request, response, "/students");
        }
    }
}
