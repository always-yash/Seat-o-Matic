package com.lostfound.member5_admin.service;

import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member5_admin.dao.ReportDAO;
import com.lostfound.member5_admin.dao.ReportDAOImpl;
import com.lostfound.model.Report;

import java.util.List;

/**
 * Report Service.
 * Manages item reporting/flagging and moderation workflow.
 */
public class ReportService {

    private final ReportDAO reportDAO = new ReportDAOImpl();

    public Report fileReport(Long reporterId, Long itemId, String reason, String description) throws Exception {
        if (reporterId == null) {
            throw new IllegalArgumentException("Reporter identification is required.");
        }
        if (!ValidationUtil.isNonEmpty(reason)) {
            throw new IllegalArgumentException("A reason must be selected or provided.");
        }

        Report report = new Report();
        report.setReportedBy(reporterId);
        report.setItemId(itemId);
        report.setReason(reason.trim());
        report.setDescription(description != null ? description.trim() : "");

        return reportDAO.create(report);
    }

    public List<Report> getAllReports() throws Exception {
        return reportDAO.findAll();
    }

    public List<Report> getReportsByStatus(String status) throws Exception {
        return reportDAO.findByStatus(status);
    }

    public boolean updateReportStatus(Long reportId, String status) throws Exception {
        if (reportId == null || !ValidationUtil.isNonEmpty(status)) {
            throw new IllegalArgumentException("Report ID and status are required.");
        }
        return reportDAO.updateStatus(reportId, status.toUpperCase());
    }
}
