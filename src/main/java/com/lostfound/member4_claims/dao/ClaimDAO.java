package com.lostfound.member4_claims.dao;

import com.lostfound.model.Claim;
import com.lostfound.model.HandoverRecord;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Claim Data Access Object Interface.
 * Handles database persistence for Claims and Handover Records.
 */
public interface ClaimDAO {

    Claim create(Claim claim) throws SQLException;

    Claim findById(Long claimId) throws SQLException;

    List<Claim> findByUser(Long claimantId) throws SQLException;

    List<Claim> findByItem(Long itemId) throws SQLException;

    List<Claim> findPendingClaims() throws SQLException;

    List<Claim> findClaimsForUserItems(Long ownerId) throws SQLException;

    boolean hasActiveClaim(Long itemId, Long claimantId) throws SQLException;

    boolean approve(Long claimId, String otp, Timestamp otpExpiry, String qrToken) throws SQLException;

    boolean reject(Long claimId) throws SQLException;

    boolean updateStatus(Long claimId, String status) throws SQLException;

    boolean executeHandoverTransaction(Long claimId, Long itemId, Long ownerId, Long receiverId,
                                       String verificationMethod, String remarks) throws SQLException;

    List<HandoverRecord> getAllHandoverRecords() throws SQLException;

    HandoverRecord getHandoverRecordByClaim(Long claimId) throws SQLException;
}
