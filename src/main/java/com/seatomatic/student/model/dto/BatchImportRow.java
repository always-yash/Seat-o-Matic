package com.seatomatic.student.model.dto;

import java.io.Serializable;

/**
 * Parsed and sanitized row data from a bulk student CSV upload.
 */
public class BatchImportRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rowNumber;
    private String enrollmentNo;
    private String name;
    private String branchCode;
    private String sectionName;
    private Integer semesterNumber;
    private String status = "ACTIVE";

    public BatchImportRow() {
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
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

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status != null && "INACTIVE".equalsIgnoreCase(status.trim())) ? "INACTIVE" : "ACTIVE";
    }
}

