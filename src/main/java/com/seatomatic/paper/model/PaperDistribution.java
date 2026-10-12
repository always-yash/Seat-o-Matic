package com.seatomatic.paper.model;

import java.sql.Timestamp;

/**
 * Domain model representing room-wise paper distribution status and counts.
 */
public class PaperDistribution {
    private Long id;
    private Long planId;
    private Long roomId;
    private Long paperId;
    private int required;
    private int buffer;
    private int distributed;
    private int collected;
    private DistributionStatus status = DistributionStatus.PENDING;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Transient presentation fields
    private String roomCode;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String setCode;
    private Long examId;

    public PaperDistribution() {
    }

    public PaperDistribution(Long planId, Long roomId, Long paperId, int required, int buffer) {
        this.planId = planId;
        this.roomId = roomId;
        this.paperId = paperId;
        this.required = required;
        this.buffer = buffer;
        this.status = DistributionStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public int getRequired() {
        return required;
    }

    public void setRequired(int required) {
        this.required = required;
    }

    public int getBuffer() {
        return buffer;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    public int getDistributed() {
        return distributed;
    }

    public void setDistributed(int distributed) {
        this.distributed = distributed;
    }

    public int getCollected() {
        return collected;
    }

    public void setCollected(int collected) {
        this.collected = collected;
    }

    public DistributionStatus getStatus() {
        return status;
    }

    public void setStatus(DistributionStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSetCode() {
        return setCode;
    }

    public void setSetCode(String setCode) {
        this.setCode = setCode;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public int getTotalRequired() {
        return required;
    }

    public boolean isReconciled() {
        return distributed > 0 && collected == distributed;
    }

    @Override
    public String toString() {
        return "PaperDistribution{" +
                "id=" + id +
                ", planId=" + planId +
                ", roomId=" + roomId +
                ", paperId=" + paperId +
                ", required=" + required +
                ", buffer=" + buffer +
                ", distributed=" + distributed +
                ", collected=" + collected +
                ", status=" + status +
                '}';
    }
}
