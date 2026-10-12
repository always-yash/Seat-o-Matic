package com.lostfound.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * HandoverRecord Entity POJO.
 */
public class HandoverRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long handoverId;
    private Long claimId;
    private Long itemId;
    private Long ownerId;
    private Long receiverId;
    private Timestamp handoverDate;
    private String verificationMethod;
    private String remarks;

    // Joined auxiliary fields
    private String itemTitle;
    private String ownerName;
    private String receiverName;

    public HandoverRecord() {
    }

    public HandoverRecord(Long handoverId, Long claimId, Long itemId, Long ownerId, Long receiverId,
                          Timestamp handoverDate, String verificationMethod, String remarks) {
        this.handoverId = handoverId;
        this.claimId = claimId;
        this.itemId = itemId;
        this.ownerId = ownerId;
        this.receiverId = receiverId;
        this.handoverDate = handoverDate;
        this.verificationMethod = verificationMethod;
        this.remarks = remarks;
    }

    public Long getHandoverId() {
        return handoverId;
    }

    public void setHandoverId(Long handoverId) {
        this.handoverId = handoverId;
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

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Timestamp getHandoverDate() {
        return handoverDate;
    }

    public void setHandoverDate(Timestamp handoverDate) {
        this.handoverDate = handoverDate;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    @Override
    public String toString() {
        return "HandoverRecord{" +
                "handoverId=" + handoverId +
                ", claimId=" + claimId +
                ", itemId=" + itemId +
                ", verificationMethod='" + verificationMethod + '\'' +
                ", handoverDate=" + handoverDate +
                '}';
    }
}
