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

/**
 * Verification Controller Servlet.
 * Handles OTP validation and QR code scanning verification.
 */
@WebServlet(name = "VerificationServlet", urlPatterns = {"/claims/verify", "/api/claims/verify"})
public class VerificationServlet extends HttpServlet {

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

            if (claim.getQrToken() != null && !claim.getQrToken().isEmpty()) {
                String qrBase64 = QRCodeUtil.generateQRCodeBase64(claim.getQrToken(), 200, 200);
                request.setAttribute("qrBase64", qrBase64);
            }

            request.setAttribute("claim", claim);
            request.setAttribute("item", item);
            request.getRequestDispatcher("/claims/otp-verification.jsp").forward(request, response);

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
        String verificationType = request.getParameter("verificationType");
        String code = request.getParameter("code");

        if (claimId == null) {
            response.sendRedirect(request.getContextPath() + "/claims?action=my-claims");
            return;
        }

        try {
            boolean verified = false;
            if ("QR".equalsIgnoreCase(verificationType)) {
                verified = claimService.verifyQrToken(claimId, code);
                adminDAO.logAudit(userId, "VERIFY_QR: Claim #" + claimId, request.getRemoteAddr());
            } else {
                verified = claimService.verifyOtp(claimId, code);
                adminDAO.logAudit(userId, "VERIFY_OTP: Claim #" + claimId, request.getRemoteAddr());
            }

            if (verified) {
                request.getSession().setAttribute("flashSuccess", "Verification successful! You can now complete the item handover.");
                response.sendRedirect(request.getContextPath() + "/claims/handover?claimId=" + claimId);
            } else {
                request.setAttribute("error", "Verification failed. Please double-check the code.");
                doGet(request, response);
            }

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            try {
                Claim claim = claimService.getClaimById(claimId);
                Item item = itemService.getItemById(claim.getItemId());
                request.setAttribute("claim", claim);
                request.setAttribute("item", item);
                if (claim.getQrToken() != null) {
                    request.setAttribute("qrBase64", QRCodeUtil.generateQRCodeBase64(claim.getQrToken(), 200, 200));
                }
            } catch (Exception ignored) {
            }
            request.getRequestDispatcher("/claims/otp-verification.jsp").forward(request, response);
        }
    }
}
