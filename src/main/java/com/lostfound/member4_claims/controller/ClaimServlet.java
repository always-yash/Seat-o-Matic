package com.lostfound.member4_claims.controller;

import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member2_items.service.ItemService;
import com.lostfound.member2_items.service.ItemServiceImpl;
import com.lostfound.member4_claims.service.ClaimService;
import com.lostfound.member4_claims.service.ClaimServiceImpl;
import com.lostfound.member4_claims.util.QRCodeUtil;
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
import java.util.List;

/**
 * Claim Controller Servlet.
 * Manages filing claims, viewing submitted claims, and approving/rejecting claims.
 */
@WebServlet(name = "ClaimServlet", urlPatterns = {"/claims", "/api/claims"})
public class ClaimServlet extends HttpServlet {

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

        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "my-claims";
        }

        try {
            switch (action) {
                case "create":
                    handleClaimForm(request, response, userId);
                    break;
                case "details":
                    handleClaimDetails(request, response, userId, role);
                    break;
                case "manage":
                    handleManageClaims(request, response, userId, role);
                    break;
                case "my-claims":
                default:
                    handleMyClaims(request, response, userId);
                    break;
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/claims/my-claims.jsp").forward(request, response);
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
        String role = (String) session.getAttribute("userRole");
        String action = request.getParameter("action");

        try {
            if ("submit".equalsIgnoreCase(action)) {
                handleSubmitClaim(request, response, userId);
            } else if ("approve".equalsIgnoreCase(action)) {
                handleApproveClaim(request, response, userId, role);
            } else if ("reject".equalsIgnoreCase(action)) {
                handleRejectClaim(request, response, userId, role);
            } else {
                response.sendRedirect(request.getContextPath() + "/claims?action=my-claims");
            }
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", e.getMessage());
            String claimId = request.getParameter("claimId");
            if (claimId != null && !claimId.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/claims?action=details&id=" + claimId);
            } else {
                response.sendRedirect(request.getContextPath() + "/claims?action=my-claims");
            }
        }
    }

    private void handleClaimForm(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        Long itemId = ValidationUtil.parseLong(request.getParameter("itemId"));
        if (itemId == null) {
            response.sendRedirect(request.getContextPath() + "/items?action=gallery");
            return;
        }
        Item item = itemService.getItemById(itemId);
        if (item == null) {
            request.getSession().setAttribute("flashError", "The requested item was not found.");
            response.sendRedirect(request.getContextPath() + "/items?action=gallery");
            return;
        }
        if (!"FOUND".equalsIgnoreCase(item.getItemType())) {
            request.getSession().setAttribute("flashError", "Claims can only be filed on items reported as FOUND.");
            response.sendRedirect(request.getContextPath() + "/items?action=view&id=" + itemId);
            return;
        }
        if (item.getUserId().equals(userId)) {
            request.getSession().setAttribute("flashError", "You cannot claim an item you reported yourself.");
            response.sendRedirect(request.getContextPath() + "/items?action=view&id=" + itemId);
            return;
        }
        request.setAttribute("item", item);
        request.getRequestDispatcher("/claims/claim-item.jsp").forward(request, response);
    }

    private void handleClaimDetails(HttpServletRequest request, HttpServletResponse response, Long userId, String role) throws Exception {
        Long claimId = ValidationUtil.parseLong(request.getParameter("id"));
        if (claimId == null) {
            response.sendRedirect(request.getContextPath() + "/claims?action=my-claims");
            return;
        }

        Claim claim = claimService.getClaimById(claimId);
        Item item = itemService.getItemById(claim.getItemId());

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        boolean isClaimant = claim.getClaimantId().equals(userId);
        boolean isItemOwner = item.getUserId().equals(userId);

        if (!isAdmin && !isClaimant && !isItemOwner) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to view this claim.");
            return;
        }

        // Generate QR Code data URL if QR token exists
        if (claim.getQrToken() != null && !claim.getQrToken().isEmpty()) {
            String qrBase64 = QRCodeUtil.generateQRCodeBase64(claim.getQrToken(), 250, 250);
            request.setAttribute("qrBase64", qrBase64);
        }

        request.setAttribute("claim", claim);
        request.setAttribute("item", item);
        request.setAttribute("isOwner", isItemOwner);
        request.setAttribute("isClaimant", isClaimant);

        request.getRequestDispatcher("/claims/claim-details.jsp").forward(request, response);
    }

    private void handleMyClaims(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        List<Claim> claims = claimService.getClaimsByUser(userId);
        request.setAttribute("claims", claims);
        request.setAttribute("viewType", "MY_SUBMITTED");
        request.getRequestDispatcher("/claims/my-claims.jsp").forward(request, response);
    }

    private void handleManageClaims(HttpServletRequest request, HttpServletResponse response, Long userId, String role) throws Exception {
        List<Claim> claims;
        if ("ADMIN".equalsIgnoreCase(role)) {
            claims = claimService.getPendingClaims();
        } else {
            claims = claimService.getClaimsForUserItems(userId);
        }
        request.setAttribute("claims", claims);
        request.setAttribute("viewType", "RECEIVED_CLAIMS");
        request.getRequestDispatcher("/claims/my-claims.jsp").forward(request, response);
    }

    private void handleSubmitClaim(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        Long itemId = ValidationUtil.parseLong(request.getParameter("itemId"));
        String reason = request.getParameter("reason");
        String proofDescription = request.getParameter("proofDescription");

        Claim createdClaim = claimService.submitClaim(itemId, userId, reason, proofDescription);
        adminDAO.logAudit(userId, "SUBMIT_CLAIM: Item #" + itemId + " Claim #" + createdClaim.getClaimId(), request.getRemoteAddr());

        request.getSession().setAttribute("flashSuccess", "Claim submitted successfully! The reporter/admin will review your proof.");
        response.sendRedirect(request.getContextPath() + "/claims?action=details&id=" + createdClaim.getClaimId());
    }

    private void handleApproveClaim(HttpServletRequest request, HttpServletResponse response, Long userId, String role) throws Exception {
        Long claimId = ValidationUtil.parseLong(request.getParameter("claimId"));
        claimService.approveClaim(claimId, userId, role);
        adminDAO.logAudit(userId, "APPROVE_CLAIM: #" + claimId, request.getRemoteAddr());

        request.getSession().setAttribute("flashSuccess", "Claim approved! A verification OTP and QR code have been issued.");
        response.sendRedirect(request.getContextPath() + "/claims?action=details&id=" + claimId);
    }

    private void handleRejectClaim(HttpServletRequest request, HttpServletResponse response, Long userId, String role) throws Exception {
        Long claimId = ValidationUtil.parseLong(request.getParameter("claimId"));
        claimService.rejectClaim(claimId, userId, role);
        adminDAO.logAudit(userId, "REJECT_CLAIM: #" + claimId, request.getRemoteAddr());

        request.getSession().setAttribute("flashInfo", "Claim has been rejected.");
        response.sendRedirect(request.getContextPath() + "/claims?action=details&id=" + claimId);
    }
}
