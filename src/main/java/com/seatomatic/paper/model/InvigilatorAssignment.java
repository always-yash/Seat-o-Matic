package com.seatomatic.paper.model;

import java.sql.Timestamp;

/**
 * Domain model representing the assignment of an invigilator to an exam room.
 */
public class InvigilatorAssignment {
    private Long id;
    private Long examId;
    private Long roomId;
    private Long invigilatorId;
    private String status = "ASSIGNED";
    private Timestamp checkInTime;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Transient presentation fields
    private String invigilatorName;
    private String invigilatorUsername;
    private String roomCode;
    private String examTitle;

    public InvigilatorAssignment() {
    }

    public InvigilatorAssignment(Long examId, Long roomId, Long invigilatorId) {
        this.examId = examId;
        this.roomId = roomId;
        this.invigilatorId = invigilatorId;
        this.status = "ASSIGNED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getInvigilatorId() {
        return invigilatorId;
    }

    public void setInvigilatorId(Long invigilatorId) {
        this.invigilatorId = invigilatorId;
    }

    /**
     * Alias for invigilatorId to align with user_id nomenclature.
     */
    public Long getUserId() {
        return invigilatorId;
    }

    public void setUserId(Long userId) {
        this.invigilatorId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Timestamp checkInTime) {
        this.checkInTime = checkInTime;
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

    public String getInvigilatorName() {
        return invigilatorName;
    }

    public void setInvigilatorName(String invigilatorName) {
        this.invigilatorName = invigilatorName;
    }

    public String getInvigilatorUsername() {
        return invigilatorUsername;
    }

    public void setInvigilatorUsername(String invigilatorUsername) {
        this.invigilatorUsername = invigilatorUsername;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    @Override
    public String toString() {
        return "InvigilatorAssignment{" +
                "id=" + id +
                ", examId=" + examId +
                ", roomId=" + roomId +
                ", invigilatorId=" + invigilatorId +
                ", status='" + status + '\'' +
                '}';
    }
}
