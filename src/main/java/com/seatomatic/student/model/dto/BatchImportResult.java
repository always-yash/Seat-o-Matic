package com.seatomatic.student.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result metrics for bulk student CSV imports.
 */
public class BatchImportResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class RowError implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int rowNumber;
        private final String enrollmentNo;
        private final String message;

        public RowError(int rowNumber, String enrollmentNo, String message) {
            this.rowNumber = rowNumber;
            this.enrollmentNo = enrollmentNo != null ? enrollmentNo : "N/A";
            this.message = message;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public String getEnrollmentNo() {
            return enrollmentNo;
        }

        public String getMessage() {
            return message;
        }
    }

    private int totalRows;
    private int importedCount;
    private int skippedCount;
    private final List<RowError> errors = new ArrayList<>();

    public BatchImportResult() {
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void incrementImported() {
        this.importedCount++;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void incrementSkipped() {
        this.skippedCount++;
    }

    public int getErrorCount() {
        return errors.size();
    }

    public void addError(int rowNumber, String enrollmentNo, String message) {
        errors.add(new RowError(rowNumber, enrollmentNo, message));
    }

    public List<RowError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String getSummaryMessage() {
        return String.format(
                "Processed %d rows: %d registered, %d skipped, %d validation issues encountered.",
                totalRows, importedCount, skippedCount, errors.size()
        );
    }
}

