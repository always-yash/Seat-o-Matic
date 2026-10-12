package com.lostfound.member5_admin.dao;

import com.lostfound.model.Report;

import java.sql.SQLException;
import java.util.List;

/**
 * Report Data Access Object Interface.
 * Handles flagging and moderation reports for inappropriate or inaccurate items.
 */
public interface ReportDAO {

    Report create(Report report) throws SQLException;

    Report findById(Long reportId) throws SQLException;

    List<Report> findAll() throws SQLException;

    List<Report> findByStatus(String status) throws SQLException;

    boolean updateStatus(Long reportId, String status) throws SQLException;

    int countOpenReports() throws SQLException;
}
