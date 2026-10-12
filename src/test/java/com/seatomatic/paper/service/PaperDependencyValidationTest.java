package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite verifying dependency guards on question paper deletion and lifecycle integrity.
 */
class PaperDependencyValidationTest {

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
    @DisplayName("Dependency Guard: Paper set deletion is blocked when active distributions exist")
    void testDeletionBlockedWithActiveDistributions() throws SQLException {
        Long paperId = 500L;
        when(mockPaperDao.hasActiveDistributions(paperId)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                paperService.deletePaperSet(paperId, 1L));

        assertTrue(ex.getMessage().contains("Active distribution records depend on this paper set"));
        verify(mockPaperDao, never()).delete(anyLong());
        verify(mockAudit, never()).publish(anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("Dependency Guard: Paper set deletion succeeds when no dependencies exist and emits audit")
    void testDeletionSucceedsWhenNoDependencies() throws Exception {
        Long paperId = 501L;
        when(mockPaperDao.hasActiveDistributions(paperId)).thenReturn(false);
        when(mockPaperDao.delete(paperId)).thenReturn(1);

        boolean deleted = paperService.deletePaperSet(paperId, 42L);

        assertTrue(deleted);
        verify(mockPaperDao).delete(paperId);
        verify(mockAudit).publish(eq("DELETE_PAPER_SET"), eq("42"), eq("QUESTION_PAPER"), eq(paperId), anyString());
    }

    @Test
    @DisplayName("Dependency Guard: Rejects null or non-positive paper ID")
    void testInvalidIdRejection() {
        assertThrows(ValidationException.class, () -> paperService.deletePaperSet(null, 1L));
        assertThrows(ValidationException.class, () -> paperService.deletePaperSet(0L, 1L));
        assertThrows(ValidationException.class, () -> paperService.deletePaperSet(-5L, 1L));
    }
}
