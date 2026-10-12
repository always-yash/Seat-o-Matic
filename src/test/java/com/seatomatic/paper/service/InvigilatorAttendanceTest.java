package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.SecurityException;
import com.seatomatic.common.security.Role;
import com.seatomatic.paper.dao.InvigilatorAssignmentDao;
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
 * Test suite verifying invigilator check-in security and attendance recording.
 */
class InvigilatorAttendanceTest {

    private InvigilatorAssignmentDao mockAssignmentDao;
    private PaperDistributionDao mockDistributionDao;
    private QuestionPaperDao mockPaperDao;
    private AuditPublisher mockAuditPublisher;
    private InvigilationService invigilationService;

    @BeforeEach
    void setUp() {
        mockAssignmentDao = mock(InvigilatorAssignmentDao.class);
        mockDistributionDao = mock(PaperDistributionDao.class);
        mockPaperDao = mock(QuestionPaperDao.class);
        mockAuditPublisher = mock(AuditPublisher.class);

        invigilationService = new InvigilationService(
                mockAssignmentDao,
                mockDistributionDao,
                mockPaperDao,
                new PaperRequirementCalculator(),
                new DistributionWorkflowService(),
                mockAuditPublisher
        );
    }

    @Test
    @DisplayName("Attendance: Assigned invigilator successfully checks in and emits audit event")
    void testAssignedInvigilatorCheckInSuccess() throws Exception {
        Long examId = 100L;
        Long roomId = 101L;
        Long invigilatorId = 55L;

        when(mockAssignmentDao.isAssigned(examId, roomId, invigilatorId)).thenReturn(true);
        when(mockAssignmentDao.recordCheckIn(examId, roomId, invigilatorId)).thenReturn(1);

        boolean checkedIn = invigilationService.recordCheckIn(examId, roomId, invigilatorId, Role.INVIGILATOR);

        assertTrue(checkedIn);
        verify(mockAssignmentDao).recordCheckIn(examId, roomId, invigilatorId);
        verify(mockAuditPublisher).publish(eq("INVIGILATOR_CHECK_IN"), eq("55"), eq("EXAM_ROOM"), eq(roomId), anyString());
    }

    @Test
    @DisplayName("Attendance: Unassigned invigilator check-in is rejected with SecurityException (IDOR)")
    void testUnassignedInvigilatorCheckInRejected() throws SQLException {
        Long examId = 100L;
        Long roomId = 102L;
        Long invigilatorId = 55L;

        when(mockAssignmentDao.isAssigned(examId, roomId, invigilatorId)).thenReturn(false);

        SecurityException ex = assertThrows(SecurityException.class, () ->
                invigilationService.recordCheckIn(examId, roomId, invigilatorId, Role.INVIGILATOR));

        assertTrue(ex.getMessage().contains("Access Denied"));
        verify(mockAssignmentDao, never()).recordCheckIn(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Attendance: Admin check-in succeeds without assignment constraint")
    void testAdminCheckInBypassesAssignmentConstraint() throws Exception {
        Long examId = 100L;
        Long roomId = 105L;
        Long adminId = 1L;

        when(mockAssignmentDao.recordCheckIn(examId, roomId, adminId)).thenReturn(1);

        boolean checkedIn = invigilationService.recordCheckIn(examId, roomId, adminId, Role.ADMIN);

        assertTrue(checkedIn);
        verify(mockAssignmentDao).recordCheckIn(examId, roomId, adminId);
    }
}
