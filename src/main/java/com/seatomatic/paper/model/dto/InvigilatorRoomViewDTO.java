package com.seatomatic.paper.model.dto;

import com.seatomatic.paper.model.DistributionStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for rendering the invigilator's room-scoped dashboard.
 * Enforces room-scoped confinement and provides requirement and reconciliation details.
 */
public class InvigilatorRoomViewDTO {
    private Long examId;
    private String examTitle;
    private String examDate;
    private String startTime;
    private String endTime;

    private Long roomId;
    private String roomCode;
    private int totalRows;
    private int totalColumns;
    private int capacity;

    private Long invigilatorId;
    private String invigilatorName;
    private String assignmentStatus;

    private List<RoomPaperRequirementDTO> requirements = new ArrayList<>();
    private List<String> studentRollNumbers = new ArrayList<>();

    private int totalAssignedStudents;
    private int totalRequiredPapers;
    private int totalDistributedPapers;
    private int totalCollectedScripts;
    private DistributionStatus overallStatus = DistributionStatus.PENDING;
    private boolean reconciled;

    public InvigilatorRoomViewDTO() {
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getTotalColumns() {
        return totalColumns;
    }

    public void setTotalColumns(int totalColumns) {
        this.totalColumns = totalColumns;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Long getInvigilatorId() {
        return invigilatorId;
    }

    public void setInvigilatorId(Long invigilatorId) {
        this.invigilatorId = invigilatorId;
    }

    public String getInvigilatorName() {
        return invigilatorName;
    }

    public void setInvigilatorName(String invigilatorName) {
        this.invigilatorName = invigilatorName;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(String assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }

    public List<RoomPaperRequirementDTO> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<RoomPaperRequirementDTO> requirements) {
        this.requirements = requirements != null ? requirements : new ArrayList<>();
        recalculateTotals();
    }

    public List<String> getStudentRollNumbers() {
        return studentRollNumbers;
    }

    public void setStudentRollNumbers(List<String> studentRollNumbers) {
        this.studentRollNumbers = studentRollNumbers != null ? studentRollNumbers : new ArrayList<>();
    }

    public int getTotalAssignedStudents() {
        return totalAssignedStudents;
    }

    public int getTotalRequiredPapers() {
        return totalRequiredPapers;
    }

    public int getTotalDistributedPapers() {
        return totalDistributedPapers;
    }

    public int getTotalCollectedScripts() {
        return totalCollectedScripts;
    }

    public DistributionStatus getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(DistributionStatus overallStatus) {
        this.overallStatus = overallStatus;
    }

    public boolean isReconciled() {
        return reconciled;
    }

    public void setReconciled(boolean reconciled) {
        this.reconciled = reconciled;
    }

    public void recalculateTotals() {
        int students = 0;
        int required = 0;
        int distributed = 0;
        int collected = 0;
        boolean allVerified = !requirements.isEmpty();
        boolean anyDistributed = false;
        boolean anyPrepared = false;

        for (RoomPaperRequirementDTO req : requirements) {
            students += req.getStudentCount();
            required += req.getTotalRequired();
            distributed += req.getDistributedCount();
            collected += req.getCollectedCount();
            if (req.getDistributionStatus() != DistributionStatus.VERIFIED) {
                allVerified = false;
            }
            if (req.getDistributionStatus() == DistributionStatus.DISTRIBUTED) {
                anyDistributed = true;
            }
            if (req.getDistributionStatus() == DistributionStatus.PREPARED) {
                anyPrepared = true;
            }
        }
        this.totalAssignedStudents = students;
        this.totalRequiredPapers = required;
        this.totalDistributedPapers = distributed;
        this.totalCollectedScripts = collected;
        this.reconciled = (distributed > 0 && distributed == collected);

        if (allVerified && !requirements.isEmpty()) {
            this.overallStatus = DistributionStatus.VERIFIED;
        } else if (anyDistributed) {
            this.overallStatus = DistributionStatus.DISTRIBUTED;
        } else if (anyPrepared) {
            this.overallStatus = DistributionStatus.PREPARED;
        } else {
            this.overallStatus = DistributionStatus.PENDING;
        }
    }
}
