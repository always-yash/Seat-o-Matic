package com.seatomatic.student.model.dto;

import java.io.Serializable;

/**
 * Metric summary representing student enrollment distribution across academic branches.
 */
public class BatchGroupStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long branchId;
    private String branchCode;
    private String branchName;
    private int totalStudents;
    private int activeStudents;
    private int inactiveStudents;

    public BatchGroupStatsDTO() {
    }

    public BatchGroupStatsDTO(Long branchId, String branchCode, String branchName, int totalStudents, int activeStudents, int inactiveStudents) {
        this.branchId = branchId;
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.totalStudents = totalStudents;
        this.activeStudents = activeStudents;
        this.inactiveStudents = inactiveStudents;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getActiveStudents() {
        return activeStudents;
    }

    public void setActiveStudents(int activeStudents) {
        this.activeStudents = activeStudents;
    }

    public int getInactiveStudents() {
        return inactiveStudents;
    }

    public void setInactiveStudents(int inactiveStudents) {
        this.inactiveStudents = inactiveStudents;
    }
}
