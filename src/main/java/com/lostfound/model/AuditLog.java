package com.lostfound.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * AuditLog Entity POJO.
 */
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long userId;
    private String action;
    private String ipAddress;
    private Timestamp createdAt;

    // Joined auxiliary fields
    private String userName;
    private String userEmail;

    public AuditLog() {
    }

    public AuditLog(Long logId, Long userId, String action, String ipAddress, Timestamp createdAt) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
