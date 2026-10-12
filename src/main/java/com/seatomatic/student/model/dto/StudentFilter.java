package com.seatomatic.student.model.dto;

import java.io.Serializable;

/**
 * Filter criteria holder for dynamic student querying, text searching, and pagination.
 */
public class StudentFilter implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_PAGE_SIZE = 15;
    public static final int MAX_PAGE_SIZE = 100;

    private String keyword;
    private Long branchId;
    private Long sectionId;
    private Long semesterId;
    private String status;
    private int page = 1;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private String sortBy = "enrollment_no";
    private String sortOrder = "ASC";

    public StudentFilter() {
    }

    public String getKeyword() {
        return keyword;
    }

    public StudentFilter setKeyword(String keyword) {
        this.keyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return this;
    }

    public Long getBranchId() {
        return branchId;
    }

    public StudentFilter setBranchId(Long branchId) {
        this.branchId = (branchId != null && branchId > 0) ? branchId : null;
        return this;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public StudentFilter setSectionId(Long sectionId) {
        this.sectionId = (sectionId != null && sectionId > 0) ? sectionId : null;
        return this;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public StudentFilter setSemesterId(Long semesterId) {
        this.semesterId = (semesterId != null && semesterId > 0) ? semesterId : null;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public StudentFilter setStatus(String status) {
        this.status = (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status))
                ? status.trim().toUpperCase() : null;
        return this;
    }

    public int getPage() {
        return page <= 0 ? 1 : page;
    }

    public StudentFilter setPage(int page) {
        this.page = page <= 0 ? 1 : page;
        return this;
    }

    public int getPageSize() {
        return pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public StudentFilter setPageSize(int pageSize) {
        this.pageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        return this;
    }

    public int getOffset() {
        return (getPage() - 1) * getPageSize();
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "enrollment_no";
    }

    public StudentFilter setSortBy(String sortBy) {
        if ("name".equalsIgnoreCase(sortBy) || "created_at".equalsIgnoreCase(sortBy) || "status".equalsIgnoreCase(sortBy)) {
            this.sortBy = sortBy.toLowerCase();
        } else {
            this.sortBy = "enrollment_no";
        }
        return this;
    }

    public String getSortOrder() {
        return "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
    }

    public StudentFilter setSortOrder(String sortOrder) {
        this.sortOrder = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        return this;
    }

    public boolean hasFilters() {
        return keyword != null || branchId != null || sectionId != null || semesterId != null || status != null;
    }
}
