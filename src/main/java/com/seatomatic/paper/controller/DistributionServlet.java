package com.seatomatic.paper.controller;

import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.common.security.CsrfTokenManager;
import com.seatomatic.common.web.BaseController;
import com.seatomatic.paper.model.dto.RoomPaperRequirementDTO;
import com.seatomatic.paper.service.DistributionWorkflowService;
import com.seatomatic.paper.service.PaperRequirementCalculator;
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
 * Controller managing room-wise paper distribution and state workflow at /distribution.
 */
@WebServlet(name = "DistributionServlet", urlPatterns = "/distribution")
public class DistributionServlet extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionServlet.class);

    private final PaperRequirementCalculator calculator;
    private final DistributionWorkflowService workflowService;
    private final CommonLookupDao lookupDao;

    public DistributionServlet() {
        this(new PaperRequirementCalculator(), new DistributionWorkflowService(), new CommonLookupDao());
    }

    public DistributionServlet(PaperRequirementCalculator calculator,
                               DistributionWorkflowService workflowService,
                               CommonLookupDao lookupDao) {
        this.calculator = calculator != null ? calculator : new PaperRequirementCalculator();
        this.workflowService = workflowService != null ? workflowService : new DistributionWorkflowService();
        this.lookupDao = lookupDao != null ? lookupDao : new CommonLookupDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long examId = longParameter(request, "examId");
        String bufferStr = parameter(request, "buffer");
        Integer buffer = bufferStr.isEmpty() ? PaperRequirementCalculator.DEFAULT_BUFFER_COUNT : null;
        if (buffer == null) {
            try {
                buffer = Integer.parseInt(bufferStr);
            } catch (NumberFormatException e) {
                buffer = PaperRequirementCalculator.DEFAULT_BUFFER_COUNT;
            }
        }

        try {
            var exams = lookupDao.findAllExams();
            if (examId == null && !exams.isEmpty()) {
                examId = exams.get(0).getId();
            }

            List<RoomPaperRequirementDTO> requirements = List.of();
            if (examId != null) {
                requirements = calculator.calculateRequirements(examId, buffer);
            }

            request.setAttribute("exams", exams);
            request.setAttribute("selectedExamId", examId);
            request.setAttribute("buffer", buffer);
            request.setAttribute("requirements", requirements);

            HttpSession session = request.getSession(false);
            if (session != null) {
                request.setAttribute("csrfToken", CsrfTokenManager.token(session));
            }

            render(request, response, "paper/distribution.jsp");
        } catch (SQLException ex) {
            LOGGER.error("Failed to load distribution dashboard", ex);
            request.setAttribute("errorMessage", "Unable to load distribution data: " + ex.getMessage());
            render(request, response, "paper/distribution.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = parameter(request, "action");
        Long examId = longParameter(request, "examId");
        HttpSession session = request.getSession(false);
        Long actorUserId = (session != null && session.getAttribute("userId") instanceof Long uid) ? uid : null;

        try {
            switch (action) {
                case "calculate" -> handleCalculate(request, response, examId, actorUserId);
                case "prepare" -> handlePrepare(request, response, examId, actorUserId);
                case "prepareAll" -> handlePrepareAll(request, response, examId, actorUserId);
                case "dispatch" -> handleDispatch(request, response, examId, actorUserId);
                case "reconcile" -> handleReconcile(request, response, examId, actorUserId);
                default -> throw new ValidationException("Unrecognized distribution action: " + action);
            }
        } catch (ValidationException ex) {
            LOGGER.warn("Distribution workflow validation failure: {}", ex.getMessage());
            request.getSession(true).setAttribute("flashError", ex.getMessage());
            redirect(request, response, "/distribution?examId=" + (examId != null ? examId : ""));
        } catch (Exception ex) {
            LOGGER.error("Error executing distribution action {}", action, ex);
            request.getSession(true).setAttribute("flashError", "Operation failed: " + ex.getMessage());
            redirect(request, response, "/distribution?examId=" + (examId != null ? examId : ""));
        }
    }

    private void handleCalculate(HttpServletRequest request, HttpServletResponse response, Long examId, Long actorUserId)
            throws Exception {
        if (examId == null) {
            throw new ValidationException("Please select an examination to calculate requirements.");
        }
        String bufferStr = parameter(request, "buffer");
        int buffer = PaperRequirementCalculator.DEFAULT_BUFFER_COUNT;
        if (!bufferStr.isEmpty()) {
            try {
                buffer = Integer.parseInt(bufferStr);
            } catch (NumberFormatException e) {
                throw new ValidationException("Buffer count must be a valid integer >= 0.");
            }
        }

        List<RoomPaperRequirementDTO> results = calculator.calculateAndPersistRequirements(examId, buffer, actorUserId);
        request.getSession(true).setAttribute("flashSuccess",
                String.format("Calculated and saved paper requirements for %d room-subject group(s) with a buffer of %d copies.",
                        results.size(), buffer));
        redirect(request, response, "/distribution?examId=" + examId + "&buffer=" + buffer);
    }

    private void handlePrepare(HttpServletRequest request, HttpServletResponse response, Long examId, Long actorUserId)
            throws Exception {
        Long distributionId = longParameter(request, "distributionId");
        if (distributionId == null) {
            throw new ValidationException("Missing distribution packet ID.");
        }
        workflowService.markPrepared(distributionId, actorUserId);
        request.getSession(true).setAttribute("flashSuccess", "Packet marked as PREPARED.");
        redirect(request, response, "/distribution?examId=" + (examId != null ? examId : ""));
    }

    private void handlePrepareAll(HttpServletRequest request, HttpServletResponse response, Long examId, Long actorUserId)
            throws Exception {
        if (examId == null) {
            throw new ValidationException("Missing exam ID for batch preparation.");
        }
        int count = workflowService.prepareAllPending(examId, actorUserId);
        request.getSession(true).setAttribute("flashSuccess", "Batch prepared " + count + " pending packet(s).");
        redirect(request, response, "/distribution?examId=" + examId);
    }

    private void handleDispatch(HttpServletRequest request, HttpServletResponse response, Long examId, Long actorUserId)
            throws Exception {
        Long distributionId = longParameter(request, "distributionId");
        String countStr = parameter(request, "distributedCount");
        if (distributionId == null) {
            throw new ValidationException("Missing distribution packet ID.");
        }
        int distributedCount;
        try {
            distributedCount = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            throw new ValidationException("Distributed count must be a valid positive integer.");
        }
        workflowService.markDistributed(distributionId, distributedCount, actorUserId);
        request.getSession(true).setAttribute("flashSuccess",
                "Packet successfully dispatched with " + distributedCount + " copies.");
        redirect(request, response, "/distribution?examId=" + (examId != null ? examId : ""));
    }

    private void handleReconcile(HttpServletRequest request, HttpServletResponse response, Long examId, Long actorUserId)
            throws Exception {
        Long distributionId = longParameter(request, "distributionId");
        String countStr = parameter(request, "collectedCount");
        if (distributionId == null) {
            throw new ValidationException("Missing distribution packet ID.");
        }
        int collectedCount;
        try {
            collectedCount = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            throw new ValidationException("Collected count must be a valid non-negative integer.");
        }
        workflowService.recordCollectionAndVerify(distributionId, collectedCount, actorUserId);
        request.getSession(true).setAttribute("flashSuccess",
                "Reconciliation verified successfully: " + collectedCount + " scripts confirmed.");
        redirect(request, response, "/distribution?examId=" + (examId != null ? examId : ""));
    }
}
