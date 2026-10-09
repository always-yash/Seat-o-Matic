package com.seatomatic.report.contract;

public interface ReportService {

    String generateRoomReport(Long examId, Long roomId);

    String generateStudentReport(Long examId);

    String generateExamSummary(Long examId);
}
