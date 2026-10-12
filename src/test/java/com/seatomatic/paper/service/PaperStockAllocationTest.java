package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.QuestionPaper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test suite verifying aggregate multi-room stock reservation and deficit detection.
 */
class PaperStockAllocationTest {

    private QuestionPaperDao mockPaperDao;
    private PaperDistributionDao mockDistDao;
    private AuditPublisher mockAudit;
    private PaperService paperService;

    @BeforeEach
    void setUp() {
        mockPaperDao = mock(QuestionPaperDao.class);
        mockDistDao = mock(PaperDistributionDao.class);
        mockAudit = mock(AuditPublisher.class);
        paperService = new PaperService(mockPaperDao, mockDistDao, new PaperRequirementCalculator(), mockAudit);
    }

    @Test
    @DisplayName("Stock Allocation: Detects aggregate deficit across multiple rooms when sum exceeds stock")
    void testAggregateDeficitDetectionAcrossMultipleRooms() throws SQLException {
        Long examId = 100L;
        QuestionPaper qp = new QuestionPaper(examId, 10L, "Set A", 70); // 70 total printed
        qp.setId(500L);

        when(mockPaperDao.findByExamId(examId)).thenReturn(List.of(qp));
        // Hall 101 needs 40, Hall 102 needs 40 => Total aggregate required is 80, but stock is only 70!
        when(mockDistDao.getAggregateRequiredForPaper(examId, 500L)).thenReturn(80);

        List<String> deficits = paperService.validateAggregateStockSufficiency(examId);

        assertNotNull(deficits);
        assertEquals(1, deficits.size(), "Should report exactly 1 deficit warning for Set A");
        assertTrue(deficits.get(0).contains("Deficit: 10"), "Should report an exact deficit of 10 copies");
        assertTrue(deficits.get(0).contains("Set 'Set A'"));
    }

    @Test
    @DisplayName("Stock Allocation: Returns empty shortfalls when total printed copies meet or exceed requirements")
    void testSufficientStockReturnsNoDeficits() throws SQLException {
        Long examId = 100L;
        QuestionPaper qp = new QuestionPaper(examId, 10L, "Set A", 100);
        qp.setId(501L);

        when(mockPaperDao.findByExamId(examId)).thenReturn(List.of(qp));
        when(mockDistDao.getAggregateRequiredForPaper(examId, 501L)).thenReturn(85); // 85 <= 100

        List<String> deficits = paperService.validateAggregateStockSufficiency(examId);

        assertNotNull(deficits);
        assertTrue(deficits.isEmpty(), "Should report zero deficits when available stock covers all rooms");
    }

    @Test
    @DisplayName("Stock Allocation: Graceful handling of null exam ID")
    void testNullExamIdHandledSafely() throws SQLException {
        List<String> deficits = paperService.validateAggregateStockSufficiency(null);
        assertNotNull(deficits);
        assertTrue(deficits.isEmpty());
    }
}
