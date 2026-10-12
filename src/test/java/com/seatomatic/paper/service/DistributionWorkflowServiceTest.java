package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.DistributionStatus;
import com.seatomatic.paper.model.PaperDistribution;
import com.seatomatic.paper.model.QuestionPaper;
import com.seatomatic.common.db.DBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test Suite TC-P02: Guarded Distribution Lifecycle Workflow & State Machine
 */
class DistributionWorkflowServiceTest {

    private PaperDistributionDao mockDistributionDao;
    private QuestionPaperDao mockPaperDao;
    private AuditPublisher mockAuditPublisher;
    private DistributionWorkflowService workflowService;

    private DataSource mockDataSource;
    private Connection mockConnection;

    @BeforeEach
    void setUp() throws SQLException {
        mockDistributionDao = mock(PaperDistributionDao.class);
        mockPaperDao = mock(QuestionPaperDao.class);
        mockAuditPublisher = mock(AuditPublisher.class);
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        DBConnection.setDataSourceForTesting(mockDataSource);

        workflowService = new DistributionWorkflowService(
                mockDistributionDao,
                mockPaperDao,
                mockAuditPublisher
        );
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        DBConnection.resetDataSourceForTesting();
    }

    @Test
    @DisplayName("TC-P02.1: Valid state progression PENDING -> PREPARED -> DISTRIBUTED -> VERIFIED")
    void testValidProgressionLifecycle() throws Exception {
        Long distId = 1L;
        Long actorId = 42L;

        PaperDistribution dist = new PaperDistribution(10L, 101L, 501L, 25, 2);
        dist.setId(distId);
        dist.setStatus(DistributionStatus.PENDING);

        QuestionPaper paper = new QuestionPaper(100L, 10L, "Set A", 100);
        paper.setId(501L);

        when(mockDistributionDao.findById(distId)).thenReturn(Optional.of(dist));
        when(mockPaperDao.findById(501L)).thenReturn(Optional.of(paper));

        // 1. PENDING -> PREPARED
        workflowService.markPrepared(distId, actorId);
        assertEquals(DistributionStatus.PREPARED, dist.getStatus());
        verify(mockAuditPublisher).publish(eq("PREPARE_PACKET"), eq("42"), eq("PAPER_DISTRIBUTION"), eq(distId), anyString());

        // 2. PREPARED -> DISTRIBUTED
        workflowService.markDistributed(distId, 25, actorId);
        assertEquals(DistributionStatus.DISTRIBUTED, dist.getStatus());
        assertEquals(25, dist.getDistributed());
        verify(mockAuditPublisher).publish(eq("DISPATCH_PAPERS"), eq("42"), eq("PAPER_DISTRIBUTION"), eq(distId), anyString());

        // 3. DISTRIBUTED -> VERIFIED (with matching 25 scripts)
        workflowService.recordCollectionAndVerify(distId, 25, actorId);
        assertEquals(DistributionStatus.VERIFIED, dist.getStatus());
        assertEquals(25, dist.getCollected());
        verify(mockAuditPublisher).publish(eq("VERIFY_RECONCILIATION"), eq("42"), eq("PAPER_DISTRIBUTION"), eq(distId), anyString());
    }

    @Test
    @DisplayName("TC-P02.2: PENDING directly to VERIFIED is strictly rejected (no jumping)")
    void testPendingDirectlyToVerifiedRejected() throws Exception {
        Long distId = 2L;
        PaperDistribution dist = new PaperDistribution(10L, 101L, 501L, 20, 2);
        dist.setId(distId);
        dist.setStatus(DistributionStatus.PENDING);

        when(mockDistributionDao.findById(distId)).thenReturn(Optional.of(dist));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                workflowService.recordCollectionAndVerify(distId, 20, 1L));

        assertTrue(ex.getMessage().contains("Illegal state transition"));
        assertEquals(DistributionStatus.PENDING, dist.getStatus());
    }

    @Test
    @DisplayName("TC-P02.3: Backward transition (DISTRIBUTED -> PREPARED) is strictly rejected")
    void testBackwardTransitionRejected() throws Exception {
        Long distId = 3L;
        PaperDistribution dist = new PaperDistribution(10L, 101L, 501L, 20, 2);
        dist.setId(distId);
        dist.setStatus(DistributionStatus.DISTRIBUTED);

        when(mockDistributionDao.findById(distId)).thenReturn(Optional.of(dist));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                workflowService.markPrepared(distId, 1L));

        assertTrue(ex.getMessage().contains("Illegal state transition"));
        assertEquals(DistributionStatus.DISTRIBUTED, dist.getStatus());
    }

    @Test
    @DisplayName("TC-P02.4: VERIFIED is terminal (no further transitions allowed)")
    void testVerifiedIsTerminal() throws Exception {
        Long distId = 4L;
        PaperDistribution dist = new PaperDistribution(10L, 101L, 501L, 20, 2);
        dist.setId(distId);
        dist.setStatus(DistributionStatus.VERIFIED);
        dist.setDistributed(20);
        dist.setCollected(20);

        when(mockDistributionDao.findById(distId)).thenReturn(Optional.of(dist));

        // Re-verifying a terminal record is rejected
        ValidationException ex = assertThrows(ValidationException.class, () ->
                workflowService.recordCollectionAndVerify(distId, 20, 1L));

        assertTrue(ex.getMessage().contains("terminal"));
        assertEquals(DistributionStatus.VERIFIED, dist.getStatus());
    }

    @Test
    @DisplayName("TC-P02.5: Reconciliation failure when collected != distributed rejects verification")
    void testReconciliationFailureRejected() throws Exception {
        Long distId = 5L;
        PaperDistribution dist = new PaperDistribution(10L, 101L, 501L, 30, 2);
        dist.setId(distId);
        dist.setStatus(DistributionStatus.DISTRIBUTED);
        dist.setDistributed(30);

        when(mockDistributionDao.findById(distId)).thenReturn(Optional.of(dist));

        // Attempting to collect 28 when 30 were distributed
        ValidationException ex = assertThrows(ValidationException.class, () ->
                workflowService.recordCollectionAndVerify(distId, 28, 1L));

        assertTrue(ex.getMessage().contains("Reconciliation failure"));
        // Status remains DISTRIBUTED, NOT VERIFIED!
        assertEquals(DistributionStatus.DISTRIBUTED, dist.getStatus());
    }

    @Test
    @DisplayName("TC-P02.6: Stock shortfall rejects preparation")
    void testStockShortfallRejectsPreparation() throws Exception {
        Long distId = 6L;
        PaperDistribution dist = new PaperDistribution(10L, 101L, 501L, 50, 2);
        dist.setId(distId);
        dist.setStatus(DistributionStatus.PENDING);

        // Only 30 printed copies available, but 50 required!
        QuestionPaper paper = new QuestionPaper(100L, 10L, "Set A", 30);
        paper.setId(501L);

        when(mockDistributionDao.findById(distId)).thenReturn(Optional.of(dist));
        when(mockPaperDao.findById(501L)).thenReturn(Optional.of(paper));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                workflowService.markPrepared(distId, 1L));

        assertTrue(ex.getMessage().contains("Stock shortfall"));
        assertEquals(DistributionStatus.PENDING, dist.getStatus());
    }
}
