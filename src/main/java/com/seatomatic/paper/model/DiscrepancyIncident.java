package com.seatomatic.paper.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain entity representing paper shortfall, damaged copies, or examination incident.
 */
public class DiscrepancyIncident implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long distributionId;
    private Long examId;
    private Long roomId;
    private String incidentType; // SHORTFALL, DAMAGED_COPIES, MISPRINT, SCRIPT_DISCREPANCY
    private int countAffected;
    private String description;
    private Long reportedBy;
    private String status = "REPORTED"; // REPORTED, INVESTIGATING, RESOLVED
    private Timestamp createdAt;
    private Timestamp resolvedAt;

    public DiscrepancyIncident() {
    }

    public DiscrepancyIncident(Long distributionId, Long examId, Long roomId, String incidentType,
                               int countAffected, String description, Long reportedBy) {
        this.distributionId = distributionId;
        this.examId = examId;
        this.roomId = roomId;
        this.incidentType = incidentType;
        this.countAffected = countAffected;
        this.description = description;
        this.reportedBy = reportedBy;
        this.status = "REPORTED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(Long distributionId) {
        this.distributionId = distributionId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public int getCountAffected() {
        return countAffected;
    }

    public void setCountAffected(int countAffected) {
        this.countAffected = countAffected;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Long reportedBy) {
        this.reportedBy = reportedBy;
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

    public Timestamp getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
