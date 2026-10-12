package com.lostfound.member4_claims.service;

import com.lostfound.config.DatabaseConnection;
import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member2_items.dao.ItemDAO;
import com.lostfound.member2_items.dao.ItemDAOImpl;
import com.lostfound.member4_claims.dao.ClaimDAO;
import com.lostfound.member4_claims.dao.ClaimDAOImpl;
import com.lostfound.model.Claim;
import com.lostfound.model.HandoverRecord;
import com.lostfound.model.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of ClaimService with security policies,
 * OTP lifecycle management, and transaction orchestration.
 */
public class ClaimServiceImpl implements ClaimService {

    private final ClaimDAO claimDAO = new ClaimDAOImpl();
    private final ItemDAO itemDAO = new ItemDAOImpl();
    private final OtpService otpService = new OtpService();

    @Override
    public Claim submitClaim(Long itemId, Long claimantId, String reason, String proofDescription) throws Exception {
        if (itemId == null || claimantId == null) {
            throw new IllegalArgumentException("Item ID and claimant ID are required.");
        }
        if (!ValidationUtil.isNonEmpty(reason) || reason.trim().length() < 5) {
            throw new IllegalArgumentException("Please state a detailed reason for claiming this item (min 5 characters).");
        }
        if (!ValidationUtil.isNonEmpty(proofDescription) || proofDescription.trim().length() < 5) {
            throw new IllegalArgumentException("Please describe identifying proof or details (min 5 characters).");
        }

        Item item = itemDAO.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found.");
        }

        // Domain rule: Only FOUND items can be claimed
        if (!"FOUND".equalsIgnoreCase(item.getItemType())) {
            throw new IllegalArgumentException("Claims can only be filed on items reported as FOUND.");
        }

        // Security check: User cannot claim their own reported item
        if (claimantId.equals(item.getUserId())) {
            throw new IllegalArgumentException("Security Policy: You cannot file a claim on an item that you reported.");
        }

        // Check if item is already closed or returned
        if ("RETURNED".equalsIgnoreCase(item.getStatus()) || "CLOSED".equalsIgnoreCase(item.getStatus())) {
            throw new IllegalStateException("This item is no longer available for claims.");
        }

        // Prevent duplicate pending claims for same user and item
        if (claimDAO.hasActiveClaim(itemId, claimantId)) {
            throw new IllegalArgumentException("You already have an active or pending claim for this item.");
        }

        Claim claim = new Claim();
        claim.setItemId(itemId);
        claim.setClaimantId(claimantId);
        claim.setReason(reason.trim());
        claim.setProofDescription(proofDescription.trim());

        Claim createdClaim = claimDAO.create(claim);

        // Notify item owner
        sendNotification(item.getUserId(), "New Claim Received",
                "A user has submitted a claim for your item \"" + item.getTitle() + "\". Please review it under Claims.");

        return createdClaim;
    }

    @Override
    public Claim getClaimById(Long claimId) throws Exception {
        if (claimId == null) {
            throw new IllegalArgumentException("Claim ID is required.");
        }
        Claim claim = claimDAO.findById(claimId);
        if (claim == null) {
            throw new IllegalArgumentException("Claim not found with ID: " + claimId);
        }
        return claim;
    }

    @Override
    public List<Claim> getClaimsByUser(Long userId) throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }
        return claimDAO.findByUser(userId);
    }

    @Override
    public List<Claim> getClaimsForUserItems(Long ownerId) throws Exception {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID is required.");
        }
        return claimDAO.findClaimsForUserItems(ownerId);
    }

    @Override
    public List<Claim> getPendingClaims() throws Exception {
        return claimDAO.findPendingClaims();
    }

    @Override
    public boolean approveClaim(Long claimId, Long approverUserId, String approverRole) throws Exception {
        Claim claim = getClaimById(claimId);
        Item item = itemDAO.findById(claim.getItemId());

        boolean isAdmin = "ADMIN".equalsIgnoreCase(approverRole);
        boolean isOwner = approverUserId != null && approverUserId.equals(item.getUserId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Forbidden: Only the item reporter or an Administrator can approve this claim.");
        }

        if (claim.getClaimantId().equals(approverUserId)) {
            throw new SecurityException("Security Policy: You cannot approve your own claim.");
        }

        // Generate 6-digit OTP
        String otp = otpService.generateOtp();
        // 5-minute expiry
        Timestamp expiry = new Timestamp(System.currentTimeMillis() + (5 * 60 * 1000));
        // Generate secure QR token
        String qrToken = "QR-CLAIM-" + claimId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        boolean approved = claimDAO.approve(claimId, otp, expiry, qrToken);

        if (approved) {
            // Dispatch OTP
            otpService.sendOtp(claim.getClaimantEmail(), otp, item.getTitle());

            // Create in-app notification
            sendNotification(claim.getClaimantId(), "Claim Approved!",
                    "Your claim for \"" + item.getTitle() + "\" was approved! Your Handover Verification OTP is: " +
                    otp + " (valid for 5 minutes). You may also present your claim QR code upon handover.");
        }

        return approved;
    }

    @Override
    public boolean rejectClaim(Long claimId, Long approverUserId, String approverRole) throws Exception {
        Claim claim = getClaimById(claimId);
        Item item = itemDAO.findById(claim.getItemId());

        boolean isAdmin = "ADMIN".equalsIgnoreCase(approverRole);
        boolean isOwner = approverUserId != null && approverUserId.equals(item.getUserId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Forbidden: Only the item reporter or an Administrator can reject this claim.");
        }

        boolean rejected = claimDAO.reject(claimId);
        if (rejected) {
            sendNotification(claim.getClaimantId(), "Claim Update",
                    "Your claim for item \"" + item.getTitle() + "\" could not be verified and was rejected.");
        }
        return rejected;
    }

    @Override
    public boolean verifyOtp(Long claimId, String enteredOtp) throws Exception {
        Claim claim = getClaimById(claimId);

        if (!"APPROVED".equalsIgnoreCase(claim.getStatus()) && !"VERIFIED".equalsIgnoreCase(claim.getStatus())) {
            throw new IllegalStateException("This claim must be approved prior to OTP verification.");
        }

        if (claim.getOtp() == null || claim.getOtpExpiry() == null) {
            throw new IllegalStateException("No active OTP found for this claim.");
        }

        if (claim.getOtpExpiry().before(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("The verification OTP has expired. Please request a fresh approval.");
        }

        if (!claim.getOtp().equals(enteredOtp != null ? enteredOtp.trim() : "")) {
            throw new IllegalArgumentException("Invalid verification OTP. Please verify and try again.");
        }

        return claimDAO.updateStatus(claimId, "VERIFIED");
    }

    @Override
    public boolean verifyQrToken(Long claimId, String enteredQrToken) throws Exception {
        Claim claim = getClaimById(claimId);

        if (claim.getQrToken() == null) {
            throw new IllegalStateException("No QR token associated with this claim.");
        }

        if (!claim.getQrToken().equals(enteredQrToken != null ? enteredQrToken.trim() : "")) {
            throw new IllegalArgumentException("Invalid QR verification token.");
        }

        return claimDAO.updateStatus(claimId, "VERIFIED");
    }

    @Override
    public boolean completeHandover(Long claimId, Long operatorUserId, String verificationMethod, String remarks) throws Exception {
        Claim claim = getClaimById(claimId);
        Item item = itemDAO.findById(claim.getItemId());

        if ("COMPLETED".equalsIgnoreCase(claim.getStatus())) {
            throw new IllegalStateException("Handover has already been completed for this claim.");
        }

        // Execute ACID database transaction
        return claimDAO.executeHandoverTransaction(
                claim.getClaimId(),
                claim.getItemId(),
                item.getUserId(),
                claim.getClaimantId(),
                verificationMethod != null ? verificationMethod : "OTP_VERIFIED",
                remarks != null ? remarks : "Handover completed successfully."
        );
    }

    @Override
    public List<HandoverRecord> getAllHandoverRecords() throws Exception {
        return claimDAO.getAllHandoverRecords();
    }

    private void sendNotification(Long userId, String title, String message) {
        String sql = "INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
