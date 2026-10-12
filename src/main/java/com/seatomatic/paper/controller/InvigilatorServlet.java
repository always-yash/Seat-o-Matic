package com.seatomatic.paper.controller;

import com.seatomatic.common.exception.SecurityException;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.common.filter.AuthorizationFilter;
import com.seatomatic.common.security.CsrfTokenManager;
import com.seatomatic.common.security.Role;
import com.seatomatic.common.web.BaseController;
import com.seatomatic.paper.model.InvigilatorAssignment;
import com.seatomatic.paper.model.dto.InvigilatorRoomViewDTO;
import com.seatomatic.paper.service.InvigilationService;
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
 * Controller providing room-scoped invigilation views and actions.
 * Strictly prevents Insecure Direct Object References (IDOR) by deriving actor identity
 * exclusively from trusted session state and verifying assignment ownership on every read and write.
 */
@WebServlet(name = "InvigilatorServlet", urlPatterns = {"/invigilator/room", "/invigilator"})
public class InvigilatorServlet extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvigilatorServlet.class);

    private final InvigilationService invigilationService;

    public InvigilatorServlet() {
        this(new InvigilationService());
    }

    public InvigilatorServlet(InvigilationService invigilationService) {
        this.invigilationService = invigilationService != null ? invigilationService : new InvigilationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            redirect(request, response, "/login");
            return;
        }

        Long authenticatedUserId = (Long) session.getAttribute("userId");
        Role authenticatedRole = AuthorizationFilter.resolveRole(session);
        if (authenticatedRole == null) {
            handleForbidden(request, response, "Valid role credential is required.");
            return;
        }

        Long examId = longParameter(request, "examId");
        Long roomId = longParameter(request, "roomId");

        // If no exam/room parameter is provided in URL, locate user's assigned rooms
        if (examId == null || roomId == null) {
            try {
                List<InvigilatorAssignment> assignments = invigilationService.getAssignmentsForInvigilator(authenticatedUserId);
                if (assignments.isEmpty()) {
                    request.setAttribute("noAssignments", true);
                    render(request, response, "invigilator/room.jsp");
                    return;
                }
                InvigilatorAssignment first = assignments.get(0);
                examId = first.getExamId();
                roomId = first.getRoomId();
            } catch (SQLException ex) {
                LOGGER.error("Failed to query invigilator assignments for user {}", authenticatedUserId, ex);
                request.setAttribute("errorMessage", "Unable to load room assignments.");
                render(request, response, "invigilator/room.jsp");
                return;
            }
        }

        try {
            // Strict room-scoped access check & view population
            InvigilatorRoomViewDTO roomView = invigilationService.getRoomView(examId, roomId, authenticatedUserId, authenticatedRole);
            request.setAttribute("roomView", roomView);
            request.setAttribute("csrfToken", CsrfTokenManager.token(session));
            render(request, response, "invigilator/room.jsp");
        } catch (SecurityException ex) {
            LOGGER.warn("IDOR violation rejected during GET: user={} exam={} room={}", authenticatedUserId, examId, roomId);
            handleForbidden(request, response, ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Failed to load invigilator room view", ex);
            request.setAttribute("errorMessage", "Error loading room workspace: " + ex.getMessage());
            render(request, response, "invigilator/room.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            redirect(request, response, "/login");
            return;
        }

        Long authenticatedUserId = (Long) session.getAttribute("userId");
        Role authenticatedRole = AuthorizationFilter.resolveRole(session);
        if (authenticatedRole == null) {
            handleForbidden(request, response, "Valid role credential is required.");
            return;
        }

        Long examId = longParameter(request, "examId");
        Long roomId = longParameter(request, "roomId");
        Long distributionId = longParameter(request, "distributionId");
        String collectedStr = parameter(request, "collectedCount");

        if (examId == null || roomId == null || distributionId == null) {
            request.getSession(true).setAttribute("flashError", "Missing required parameters.");
            redirect(request, response, "/invigilator/room?examId=" + (examId != null ? examId : "") + "&roomId=" + (roomId != null ? roomId : ""));
            return;
        }

        int collectedCount;
        try {
            collectedCount = Integer.parseInt(collectedStr);
        } catch (NumberFormatException e) {
            request.getSession(true).setAttribute("flashError", "Collected scripts must be a valid integer.");
            redirect(request, response, "/invigilator/room?examId=" + examId + "&roomId=" + roomId);
            return;
        }

        try {
            // Re-verify authorization and perform collection verification
            invigilationService.submitRoomCollection(examId, roomId, distributionId, collectedCount,
                    authenticatedUserId, authenticatedRole);
            request.getSession(true).setAttribute("flashSuccess",
                    "Collected " + collectedCount + " answer scripts recorded and verified successfully.");
            redirect(request, response, "/invigilator/room?examId=" + examId + "&roomId=" + roomId);
        } catch (SecurityException ex) {
            LOGGER.warn("IDOR violation blocked on POST: user={} exam={} room={} dist={}",
                    authenticatedUserId, examId, roomId, distributionId);
            handleForbidden(request, response, ex.getMessage());
        } catch (ValidationException ex) {
            LOGGER.warn("Reconciliation failure during collection submission: {}", ex.getMessage());
            request.getSession(true).setAttribute("flashError", ex.getMessage());
            redirect(request, response, "/invigilator/room?examId=" + examId + "&roomId=" + roomId);
        } catch (Exception ex) {
            LOGGER.error("Failed to submit room collection", ex);
            request.getSession(true).setAttribute("flashError", "Collection submission failed: " + ex.getMessage());
            redirect(request, response, "/invigilator/room?examId=" + examId + "&roomId=" + roomId);
        }
    }

    private void handleForbidden(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
    }
}
