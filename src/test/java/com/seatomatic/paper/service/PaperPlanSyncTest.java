package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.dto.RoomPaperRequirementDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test suite verifying seating plan re-synchronization and orphan packet cleanup.
 */
class PaperPlanSyncTest {

    private QuestionPaperDao mockPaperDao;
    private PaperDistributionDao mockDistDao;
    private AuditPublisher mockAudit;
    private PaperRequirementCalculator calculator;

    @BeforeEach
    void setUp() {
        mockPaperDao = mock(QuestionPaperDao.class);
        mockDistDao = mock(PaperDistributionDao.class);
        mockAudit = mock(AuditPublisher.class);

        calculator = Mockito.spy(new PaperRequirementCalculator(mockPaperDao, mockDistDao, mockAudit, null));
    }

    @Test
    @DisplayName("Plan Sync: Prunes orphaned pending records and emits audit event")
    void testSyncDistributionWithActivePlanPrunesOrphans() throws Exception {
        Long examId = 100L;
        Long planId = 10L;

        doReturn(planId).when(calculator).findActivePlanId(examId);

        RoomPaperRequirementDTO req1 = new RoomPaperRequirementDTO();
        req1.setRoomId(101L);
        RoomPaperRequirementDTO req2 = new RoomPaperRequirementDTO();
        req2.setRoomId(102L);

        doReturn(List.of(req1, req2)).when(calculator).calculateRequirements(examId, null);
        when(mockDistDao.deleteOrphanedPendingRecords(eq(planId), anyList())).thenReturn(2);

        int pruned = calculator.syncDistributionWithActivePlan(examId, 42L);

        assertEquals(2, pruned, "Should report 2 orphaned room packets pruned");
        verify(mockDistDao).deleteOrphanedPendingRecords(eq(planId), eq(List.of(101L, 102L)));
        verify(mockAudit).publish(eq("SYNC_DISTRIBUTION_PLAN"), eq("42"), eq("EXAM"), eq(examId), anyString());
    }

    @Test
    @DisplayName("Plan Sync: Gracefully returns 0 if no active plan exists")
    void testSyncWithNoPlanReturnsZero() throws Exception {
        Long examId = 200L;
        doReturn(null).when(calculator).findActivePlanId(examId);

        int pruned = calculator.syncDistributionWithActivePlan(examId, 42L);

        assertEquals(0, pruned);
        verify(mockDistDao, never()).deleteOrphanedPendingRecords(anyLong(), anyList());
    }
}
