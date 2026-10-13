package com.seatomatic.analysis;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
public class SeatingReportApiController {
    private static final MediaType CSV_UTF8 = MediaType.parseMediaType("text/csv;charset=UTF-8");

    private final SeatingReportService reports;

    public SeatingReportApiController(SeatingReportService reports) {
        this.reports = reports;
    }

    @GetMapping(value = "/locked-seating.csv", produces = "text/csv")
    public ResponseEntity<byte[]> lockedSeatingCsv() {
        return ResponseEntity.ok()
                .contentType(CSV_UTF8)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=locked-seating-plans.csv")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(reports.lockedPlansCsv());
    }
}
