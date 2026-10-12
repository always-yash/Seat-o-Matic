package com.seatomatic.student.model.dto;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Data Transfer Object representing a comprehensive student profile view,
 * aggregating academic branch, section, semester, and activity counts.
 */
public class StudentDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String enrollmentNo;
    private String name;
    private Long sectionId;
    private String sectionName;
    private Long branchId;
    private String branchCode;
    private String branchName;
    private Long semesterId;
    private Integer semesterNumber;
    private String semesterName;
    private String status;
    private Timestamp createdAt;
    private int examCount;
    private int seatAssignmentCount;

    public StudentDetailDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(String enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
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

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
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

    public int getExamCount() {
        return examCount;
    }

    public void setExamCount(int examCount) {
        this.examCount = examCount;
    }

    public int getSeatAssignmentCount() {
        return seatAssignmentCount;
    }

    public void setSeatAssignmentCount(int seatAssignmentCount) {
        this.seatAssignmentCount = seatAssignmentCount;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public String getBatchDisplay() {
        String b = branchCode != null ? branchCode : "N/A";
        String sec = sectionName != null ? sectionName : "";
        String sem = semesterNumber != null ? "Sem " + semesterNumber : "";
        return b + (sec.isEmpty() ? "" : " · " + sec) + (sem.isEmpty() ? "" : " · " + sem);
    }
}
