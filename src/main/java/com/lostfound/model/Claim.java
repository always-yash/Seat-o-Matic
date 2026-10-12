package com.lostfound.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Claim Entity POJO.
 */
public class Claim implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long claimId;
    private Long itemId;
    private Long claimantId;
    private String reason;
    private String proofDescription;
    private String status; // PENDING, APPROVED, REJECTED, VERIFIED, COMPLETED
    private String otp;
    private Timestamp otpExpiry;
    private String qrToken;
    private Timestamp createdAt;

    // Joined auxiliary presentation fields
    private String itemTitle;
    private String itemType;
    private String itemLocation;
    private String itemImage;
    private String claimantName;
    private String claimantEmail;
    private String claimantPhone;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    public Claim() {
    }

    public Claim(Long claimId, Long itemId, Long claimantId, String reason, String proofDescription,
                 String status, String otp, Timestamp otpExpiry, String qrToken, Timestamp createdAt) {
        this.claimId = claimId;
        this.itemId = itemId;
        this.claimantId = claimantId;
        this.reason = reason;
        this.proofDescription = proofDescription;
        this.status = status;
        this.otp = otp;
        this.otpExpiry = otpExpiry;
        this.qrToken = qrToken;
        this.createdAt = createdAt;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(Long claimantId) {
        this.claimantId = claimantId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getProofDescription() {
        return proofDescription;
    }

    public void setProofDescription(String proofDescription) {
        this.proofDescription = proofDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public Timestamp getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(Timestamp otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

    public String getClaimantEmail() {
        return claimantEmail;
    }

    public void setClaimantEmail(String claimantEmail) {
        this.claimantEmail = claimantEmail;
    }

    public String getClaimantPhone() {
        return claimantPhone;
    }

    public void setClaimantPhone(String claimantPhone) {
        this.claimantPhone = claimantPhone;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    @Override
    public String toString() {
        return "Claim{" +
                "claimId=" + claimId +
                ", itemId=" + itemId +
                ", claimantId=" + claimantId +
                ", status='" + status + '\'' +
                '}';
    }
}
