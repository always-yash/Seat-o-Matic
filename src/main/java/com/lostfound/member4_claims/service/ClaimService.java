package com.lostfound.member4_claims.service;

import com.lostfound.model.Claim;
import com.lostfound.model.HandoverRecord;

import java.util.List;

/**
 * Claim Service Interface.
 * Governs claim creation, multi-factor verification (OTP/QR),
 * and transactional item handover.
 */
public interface ClaimService {

    Claim submitClaim(Long itemId, Long claimantId, String reason, String proofDescription) throws Exception;

    Claim getClaimById(Long claimId) throws Exception;

    List<Claim> getClaimsByUser(Long userId) throws Exception;

    List<Claim> getClaimsForUserItems(Long ownerId) throws Exception;

    List<Claim> getPendingClaims() throws Exception;

    boolean approveClaim(Long claimId, Long approverUserId, String approverRole) throws Exception;

    boolean rejectClaim(Long claimId, Long approverUserId, String approverRole) throws Exception;

    boolean verifyOtp(Long claimId, String enteredOtp) throws Exception;

    boolean verifyQrToken(Long claimId, String enteredQrToken) throws Exception;

    boolean completeHandover(Long claimId, Long operatorUserId, String verificationMethod, String remarks) throws Exception;

    List<HandoverRecord> getAllHandoverRecords() throws Exception;
}
