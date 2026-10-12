package com.seatomatic.paper.model.dto;

import com.seatomatic.paper.model.DistributionStatus;

/**
 * Data Transfer Object representing the calculated question paper requirement
 * for a specific (Room, Subject) pair under an examination session.
 */
public class RoomPaperRequirementDTO {
    private Long examId;
    private Long roomId;
    private String roomCode;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private int studentCount;
    private int bufferCount;
    private int totalRequired;
    private Long paperId;
    private String paperSetCode;
    private int availableStock;
    private DistributionStatus distributionStatus = DistributionStatus.PENDING;
    private int distributedCount;
    private int collectedCount;

    public RoomPaperRequirementDTO() {
    }

    public RoomPaperRequirementDTO(Long examId, Long roomId, String roomCode,
                                   Long subjectId, String subjectCode, String subjectName,
                                   int studentCount, int bufferCount, int totalRequired) {
        this.examId = examId;
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.studentCount = studentCount;
        this.bufferCount = bufferCount;
        this.totalRequired = totalRequired;
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

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public int getBufferCount() {
        return bufferCount;
    }

    public void setBufferCount(int bufferCount) {
        this.bufferCount = bufferCount;
    }

    public int getTotalRequired() {
        return totalRequired;
    }

    public void setTotalRequired(int totalRequired) {
        this.totalRequired = totalRequired;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public String getPaperSetCode() {
        return paperSetCode;
    }

    public void setPaperSetCode(String paperSetCode) {
        this.paperSetCode = paperSetCode;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public DistributionStatus getDistributionStatus() {
        return distributionStatus;
    }

    public void setDistributionStatus(DistributionStatus distributionStatus) {
        this.distributionStatus = distributionStatus;
    }

    public int getDistributedCount() {
        return distributedCount;
    }

    public void setDistributedCount(int distributedCount) {
        this.distributedCount = distributedCount;
    }

    public int getCollectedCount() {
        return collectedCount;
    }

    public void setCollectedCount(int collectedCount) {
        this.collectedCount = collectedCount;
    }

    public boolean isStockSufficient() {
        return availableStock >= totalRequired;
    }

    @Override
    public String toString() {
        return "RoomPaperRequirementDTO{" +
                "roomCode='" + roomCode + '\'' +
                ", subjectCode='" + subjectCode + '\'' +
                ", studentCount=" + studentCount +
                ", bufferCount=" + bufferCount +
                ", totalRequired=" + totalRequired +
                ", status=" + distributionStatus +
                '}';
    }
}
