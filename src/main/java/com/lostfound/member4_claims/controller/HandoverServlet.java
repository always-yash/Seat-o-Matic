package com.lostfound.member4_claims.controller;

import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member2_items.service.ItemService;
import com.lostfound.member2_items.service.ItemServiceImpl;
import com.lostfound.member4_claims.service.ClaimService;
import com.lostfound.member4_claims.service.ClaimServiceImpl;
import com.lostfound.member5_admin.dao.AdminDAO;
import com.lostfound.member5_admin.dao.AdminDAOImpl;
import com.lostfound.model.Claim;
import com.lostfound.model.Item;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handover Controller Servlet.
 * Completes final physical verification, logs handover records,
 * and sets item status to RETURNED within an ACID database transaction.
 */
@WebServlet(name = "HandoverServlet", urlPatterns = {"/claims/handover", "/api/handover"})
public class HandoverServlet extends HttpServlet {

    private final ClaimService claimService = new ClaimServiceImpl();
    private final ItemService itemService = new ItemServiceImpl();
    private final AdminDAO adminDAO = new AdminDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please+log+in+first");
            return;
        }

        Long claimId = ValidationUtil.parseLong(request.getParameter("claimId"));
        if (claimId == null) {
            response.sendRedirect(request.getContextPath() + "/claims?action=my-claims");
            return;
        }

        try {
            Claim claim = claimService.getClaimById(claimId);
            Item item = itemService.getItemById(claim.getItemId());

            request.setAttribute("claim", claim);
            request.setAttribute("item", item);
            request.getRequestDispatcher("/claims/handover.jsp").forward(request, response);

        } catch (Exception e) {
            request.getSession().setAttribute("flashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/claims?action=my-claims");
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
        Long claimId = ValidationUtil.parseLong(request.getParameter("claimId"));
        String verificationMethod = request.getParameter("verificationMethod");
        String remarks = request.getParameter("remarks");

        if (claimId == null) {
            response.sendRedirect(request.getContextPath() + "/claims?action=my-claims");
            return;
        }

        try {
            boolean done = claimService.completeHandover(claimId, userId, verificationMethod, remarks);
            if (done) {
                adminDAO.logAudit(userId, "HANDOVER_TRANSACTION_COMPLETE: Claim #" + claimId, request.getRemoteAddr());
                request.getSession().setAttribute("flashSuccess", "Item handover successfully completed! Status is now updated to RETURNED.");
                response.sendRedirect(request.getContextPath() + "/claims?action=details&id=" + claimId);
            } else {
                request.setAttribute("error", "Failed to finalize handover.");
                doGet(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        }
    }
}
