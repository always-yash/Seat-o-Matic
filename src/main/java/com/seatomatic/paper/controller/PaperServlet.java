package com.seatomatic.paper.controller;

import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.common.security.CsrfTokenManager;
import com.seatomatic.common.web.BaseController;
import com.seatomatic.exam.model.Exam;
import com.seatomatic.paper.model.QuestionPaper;
import com.seatomatic.paper.service.PaperService;
import com.seatomatic.student.model.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller managing question paper sets at /papers.
 * Supports viewing existing sets, adding new sets (A, B, C), and updating stock.
 */
@WebServlet(name = "PaperServlet", urlPatterns = "/papers")
public class PaperServlet extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaperServlet.class);

    private final PaperService paperService;
    private final CommonLookupDao lookupDao;

    public PaperServlet() {
        this(new PaperService(), new CommonLookupDao());
    }

    public PaperServlet(PaperService paperService, CommonLookupDao lookupDao) {
        this.paperService = paperService != null ? paperService : new PaperService();
        this.lookupDao = lookupDao != null ? lookupDao : new CommonLookupDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long examId = longParameter(request, "examId");
        try {
            List<QuestionPaper> papers = (examId != null && examId > 0)
                    ? paperService.getPapersForExam(examId)
                    : paperService.getAllPapers();

            request.setAttribute("papers", papers);
            request.setAttribute("selectedExamId", examId);
            request.setAttribute("exams", lookupDao.findAllExams());
            request.setAttribute("subjects", lookupDao.findAllSubjects());

            HttpSession session = request.getSession(false);
            if (session != null) {
                request.setAttribute("csrfToken", CsrfTokenManager.token(session));
            }

            render(request, response, "paper/sets.jsp");
        } catch (SQLException ex) {
            LOGGER.error("Failed to fetch question paper records", ex);
            request.setAttribute("errorMessage", "Unable to load question papers from database.");
            render(request, response, "paper/sets.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = parameter(request, "action");
        HttpSession session = request.getSession(false);
        Long actorUserId = (session != null && session.getAttribute("userId") instanceof Long uid) ? uid : null;

        try {
            if ("updateStock".equalsIgnoreCase(action)) {
                handleUpdateStock(request, response, actorUserId);
            } else {
                handleCreateSet(request, response, actorUserId);
            }
        } catch (ValidationException ex) {
            LOGGER.warn("Validation error on paper set operation: {}", ex.getMessage());
            request.getSession(true).setAttribute("flashError", ex.getMessage());
            redirect(request, response, "/papers" + (request.getParameter("examId") != null ? "?examId=" + request.getParameter("examId") : ""));
        } catch (SQLException ex) {
            LOGGER.error("Database error during paper set operation", ex);
            request.getSession(true).setAttribute("flashError", "Database error: " + ex.getMessage());
            redirect(request, response, "/papers");
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during paper set operation", ex);
            request.getSession(true).setAttribute("flashError", "An unexpected error occurred.");
            redirect(request, response, "/papers");
        }
    }

    private void handleCreateSet(HttpServletRequest request, HttpServletResponse response, Long actorUserId)
            throws Exception {
        Long examId = longParameter(request, "examId");
        Long subjectId = longParameter(request, "subjectId");
        String setCode = parameter(request, "setCode");
        String copiesStr = parameter(request, "totalCopies");

        if (examId == null) {
            throw new ValidationException("Please select an examination.");
        }
        if (subjectId == null) {
            throw new ValidationException("Please select a subject.");
        }
        if (setCode.isEmpty()) {
            throw new ValidationException("Please enter a paper set code (e.g. Set A, Set B).");
        }
        int totalCopies = 0;
        try {
            totalCopies = Integer.parseInt(copiesStr);
        } catch (NumberFormatException e) {
            throw new ValidationException("Total printed copies must be a valid integer.");
        }

        QuestionPaper paper = paperService.createPaperSet(examId, subjectId, setCode, totalCopies, actorUserId);
        request.getSession(true).setAttribute("flashSuccess",
                "Successfully added Paper Set '" + paper.getSetCode() + "' with " + totalCopies + " copies.");
        redirect(request, response, "/papers?examId=" + examId);
    }

    private void handleUpdateStock(HttpServletRequest request, HttpServletResponse response, Long actorUserId)
            throws Exception {
        Long paperId = longParameter(request, "paperId");
        String copiesStr = parameter(request, "totalCopies");
        Long examId = longParameter(request, "examId");

        if (paperId == null) {
            throw new ValidationException("Missing question paper ID.");
        }
        int totalCopies = 0;
        try {
            totalCopies = Integer.parseInt(copiesStr);
        } catch (NumberFormatException e) {
            throw new ValidationException("Total printed copies must be a valid integer.");
        }

        paperService.updateStock(paperId, totalCopies, actorUserId);
        request.getSession(true).setAttribute("flashSuccess", "Stock count updated successfully to " + totalCopies + ".");
        redirect(request, response, "/papers" + (examId != null ? "?examId=" + examId : ""));
    }
}
