package com.lostfound.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Report Entity POJO (Flagged / Reported items moderation).
 */
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long reportId;
    private Long reportedBy;
    private Long itemId;
    private String reason;
    private String description;
    private String status; // OPEN, REVIEWED, RESOLVED
    private Timestamp createdAt;

    // Joined auxiliary fields
    private String reporterName;
    private String reporterEmail;
    private String itemTitle;

    public Report() {
    }

    public Report(Long reportId, Long reportedBy, Long itemId, String reason, String description, String status, Timestamp createdAt) {
        this.reportId = reportId;
        this.reportedBy = reportedBy;
        this.itemId = itemId;
        this.reason = reason;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Long reportedBy) {
        this.reportedBy = reportedBy;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    @Override
    public String toString() {
        return "Report{" +
                "reportId=" + reportId +
                ", reportedBy=" + reportedBy +
                ", itemId=" + itemId +
                ", status='" + status + '\'' +
                '}';
    }
}
