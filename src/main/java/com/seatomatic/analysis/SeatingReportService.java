package com.seatomatic.analysis;

import com.seatomatic.core.SeatingService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatingReportService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String HEADER = "Exam code,Exam name,Starts at,Room,Seat row,Seat column,Student ID,Student name,Branch,Section,Semester";

    private final SeatingService seating;
    private final AuditService audit;

    public SeatingReportService(SeatingService seating, AuditService audit) {
        this.seating = seating;
        this.audit = audit;
    }

    @Transactional
    public byte[] lockedPlansCsv() {
        List<SeatingService.LockedPlanReportRow> rows = seating.lockedPlanReportRows();
        StringBuilder csv = new StringBuilder("\uFEFF").append(HEADER).append("\r\n");
        for (SeatingService.LockedPlanReportRow row : rows) {
            appendRow(csv, row);
        }
        audit.record("LOCKED_SEATING_REPORT_EXPORTED", "Seating report", rows.size() + " assigned seats exported");
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private void appendRow(StringBuilder csv, SeatingService.LockedPlanReportRow row) {
        csv.append(cell(row.examCode())).append(',')
                .append(cell(row.examName())).append(',')
                .append(cell(row.startsAt() == null ? "" : DATE_TIME.format(row.startsAt()))).append(',')
                .append(cell(row.roomName())).append(',')
                .append(cell(Integer.toString(row.seatRow()))).append(',')
                .append(cell(Integer.toString(row.seatColumn()))).append(',')
                .append(cell(row.studentId())).append(',')
                .append(cell(row.studentName())).append(',')
                .append(cell(row.branch())).append(',')
                .append(cell(row.section())).append(',')
                .append(cell(Integer.toString(row.semester())))
                .append("\r\n");
    }

    private String cell(String value) {
        String safe = value == null ? "" : value;
        if (safe.stripLeading().matches("^[=+@\\-\\t\\r\\n].*")) {
            safe = "'" + safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
