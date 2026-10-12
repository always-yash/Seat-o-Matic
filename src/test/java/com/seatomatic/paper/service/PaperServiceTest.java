package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.QuestionPaper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test Suite for PaperService & PaperQueryService contract integration.
 */
class PaperServiceTest {

    private QuestionPaperDao mockPaperDao;
    private PaperDistributionDao mockDistributionDao;
    private PaperRequirementCalculator mockCalculator;
    private AuditPublisher mockAuditPublisher;
    private PaperService paperService;

    @BeforeEach
    void setUp() {
        mockPaperDao = mock(QuestionPaperDao.class);
        mockDistributionDao = mock(PaperDistributionDao.class);
        mockCalculator = mock(PaperRequirementCalculator.class);
        mockAuditPublisher = mock(AuditPublisher.class);

        paperService = new PaperService(
                mockPaperDao,
                mockDistributionDao,
                mockCalculator,
                mockAuditPublisher
        );
    }

    @Test
    @DisplayName("Create paper set successfully with normalization")
    void testCreatePaperSetSuccess() throws Exception {
        when(mockPaperDao.existsDuplicateSet(100L, 10L, "SET A", null)).thenReturn(false);
        when(mockPaperDao.insert(any(QuestionPaper.class))).thenReturn(123L);

        QuestionPaper result = paperService.createPaperSet(100L, 10L, "set a", 80, 1L);

        assertNotNull(result);
        assertEquals(123L, result.getId());
        assertEquals("SET A", result.getSetCode());
        assertEquals(80, result.getTotalCopies());
        verify(mockAuditPublisher).publish(eq("CREATE_PAPER_SET"), eq("1"), eq("QUESTION_PAPER"), eq(123L), anyString());
    }

    @Test
    @DisplayName("Duplicate paper set is rejected with ValidationException")
    void testDuplicatePaperSetRejected() throws SQLException {
        when(mockPaperDao.existsDuplicateSet(100L, 10L, "SET A", null)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                paperService.createPaperSet(100L, 10L, "Set A", 80, 1L));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(mockPaperDao, never()).insert(any());
    }

    @Test
    @DisplayName("Invalid paper inputs (blank set, negative copies) are rejected")
    void testInvalidPaperInputsRejected() {
        assertThrows(ValidationException.class, () ->
                paperService.createPaperSet(null, 10L, "Set A", 80, 1L));

        assertThrows(ValidationException.class, () ->
                paperService.createPaperSet(100L, null, "Set A", 80, 1L));

        assertThrows(ValidationException.class, () ->
                paperService.createPaperSet(100L, 10L, "", 80, 1L));

        assertThrows(ValidationException.class, () ->
                paperService.createPaperSet(100L, 10L, "Set A", -10, 1L));
    }

    @Test
    @DisplayName("Update stock updates value and publishes audit event")
    void testUpdateStockSuccess() throws Exception {
        QuestionPaper existing = new QuestionPaper(100L, 10L, "Set A", 50);
        existing.setId(10L);

        when(mockPaperDao.findById(10L)).thenReturn(Optional.of(existing));

        paperService.updateStock(10L, 120, 1L);

        verify(mockPaperDao).updateStock(10L, 120);
        verify(mockAuditPublisher).publish(eq("UPDATE_PAPER_STOCK"), eq("1"), eq("QUESTION_PAPER"), eq(10L), anyString());
    }

    @Test
    @DisplayName("PaperQueryService contract: getRequiredCopies delegates to DAO")
    void testPaperQueryServiceGetRequiredCopies() throws SQLException {
        when(mockDistributionDao.getRequiredCopies(101L, 10L)).thenReturn(35);

        int copies = paperService.getRequiredCopies(101L, 10L);
        assertEquals(35, copies);
    }

    @Test
    @DisplayName("PaperQueryService contract: isDistributionComplete delegates to DAO")
    void testPaperQueryServiceIsDistributionComplete() throws SQLException {
        when(mockDistributionDao.isDistributionCompleteForRoom(101L)).thenReturn(true);
        when(mockDistributionDao.isDistributionCompleteForRoom(102L)).thenReturn(false);

        assertTrue(paperService.isDistributionComplete(101L));
        assertFalse(paperService.isDistributionComplete(102L));
    }
}
