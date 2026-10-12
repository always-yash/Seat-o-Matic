package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.SecurityException;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.common.security.Role;
import com.seatomatic.paper.dao.InvigilatorAssignmentDao;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.PaperDistribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test Suite TC-P03: Invigilator Authorization & Room-Scoped IDOR Access Control
 */
class InvigilationServiceSecurityTest {

    private InvigilatorAssignmentDao mockAssignmentDao;
    private PaperDistributionDao mockDistributionDao;
    private QuestionPaperDao mockPaperDao;
    private DistributionWorkflowService mockWorkflowService;
    private AuditPublisher mockAuditPublisher;
    private InvigilationService invigilationService;

    @BeforeEach
    void setUp() {
        mockAssignmentDao = mock(InvigilatorAssignmentDao.class);
        mockDistributionDao = mock(PaperDistributionDao.class);
        mockPaperDao = mock(QuestionPaperDao.class);
        mockWorkflowService = mock(DistributionWorkflowService.class);
        mockAuditPublisher = mock(AuditPublisher.class);

        invigilationService = new InvigilationService(
                mockAssignmentDao,
                mockDistributionDao,
                mockPaperDao,
                null,
                mockWorkflowService,
                mockAuditPublisher
        );
    }

    @Test
    @DisplayName("TC-P03.1: Assigned invigilator successfully passes room verification")
    void testAssignedInvigilatorPermitted() throws Exception {
        Long examId = 100L;
        Long roomId = 101L;
        Long userId = 77L;

        when(mockAssignmentDao.isAssigned(examId, roomId, userId)).thenReturn(true);

        assertDoesNotThrow(() ->
                invigilationService.verifyAccess(examId, roomId, userId, Role.INVIGILATOR));
    }

    @Test
    @DisplayName("TC-P03.2: Unassigned invigilator receives SecurityException (HTTP 403 / IDOR blocked)")
    void testUnassignedInvigilatorDenied() throws Exception {
        Long examId = 100L;
        Long targetRoomId = 102L; // Target Room 102
        Long userId = 77L;        // Invigilator 77 (only assigned to 101, NOT 102!)

        when(mockAssignmentDao.isAssigned(examId, targetRoomId, userId)).thenReturn(false);

        SecurityException ex = assertThrows(SecurityException.class, () ->
                invigilationService.verifyAccess(examId, targetRoomId, userId, Role.INVIGILATOR));

        assertTrue(ex.getMessage().contains("Access Denied"));
        verify(mockAuditPublisher).publish(eq("IDOR_ACCESS_DENIED"), eq("77"), eq("EXAM_ROOM"), eq(targetRoomId), anyString());
    }

    @Test
    @DisplayName("TC-P03.3: Unauthenticated access (missing userId or role) is rejected")
    void testUnauthenticatedAccessRejected() {
        SecurityException ex1 = assertThrows(SecurityException.class, () ->
                invigilationService.verifyAccess(100L, 101L, null, Role.INVIGILATOR));
        assertTrue(ex1.getMessage().contains("Authentication is required"));

        SecurityException ex2 = assertThrows(SecurityException.class, () ->
                invigilationService.verifyAccess(100L, 101L, 77L, null));
        assertTrue(ex2.getMessage().contains("Authentication is required"));
    }

    @Test
    @DisplayName("TC-P03.4: Manipulating exam ID or room ID fails authorization")
    void testManipulatedIdsFailAuthorization() throws Exception {
        Long examId = 100L;
        Long roomId = 101L;
        Long userId = 77L;

        when(mockAssignmentDao.isAssigned(examId, roomId, userId)).thenReturn(true);
        // Manipulated examId = 999
        when(mockAssignmentDao.isAssigned(999L, roomId, userId)).thenReturn(false);

        assertThrows(SecurityException.class, () ->
                invigilationService.verifyAccess(999L, roomId, userId, Role.INVIGILATOR));

        // Manipulated roomId = 999
        when(mockAssignmentDao.isAssigned(examId, 999L, userId)).thenReturn(false);

        assertThrows(SecurityException.class, () ->
                invigilationService.verifyAccess(examId, 999L, userId, Role.INVIGILATOR));
    }

    @Test
    @DisplayName("TC-P03.5: Unauthorized POST collection is blocked and cross-room submission rejected")
    void testPostCollectionUnauthorizedRejected() throws Exception {
        Long examId = 100L;
        Long roomId = 101L;
        Long userId = 77L;
        Long distributionId = 50L;

        // User is assigned to room 101
        when(mockAssignmentDao.isAssigned(examId, roomId, userId)).thenReturn(true);

        // But distribution record belongs to room 202 (cross-room manipulation!)
        PaperDistribution dist = new PaperDistribution(1L, 202L, 5L, 20, 2);
        dist.setId(distributionId);
        when(mockDistributionDao.findById(distributionId)).thenReturn(Optional.of(dist));

        SecurityException ex = assertThrows(SecurityException.class, () ->
                invigilationService.submitRoomCollection(examId, roomId, distributionId, 20, userId, Role.INVIGILATOR));

        assertTrue(ex.getMessage().contains("Cross-room tampering blocked"));
        verify(mockWorkflowService, never()).recordCollectionAndVerify(anyLong(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("TC-P03.6: ADMIN and FACULTY have supervisory access")
    void testSupervisoryAccess() {
        assertDoesNotThrow(() ->
                invigilationService.verifyAccess(100L, 101L, 1L, Role.ADMIN));

        assertDoesNotThrow(() ->
                invigilationService.verifyAccess(100L, 101L, 2L, Role.FACULTY));
    }
}
