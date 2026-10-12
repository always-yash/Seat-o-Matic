package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.security.Role;
import com.seatomatic.paper.dao.DiscrepancyIncidentDao;
import com.seatomatic.paper.dao.InvigilatorAssignmentDao;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.DiscrepancyIncident;
import com.seatomatic.paper.model.DistributionStatus;
import com.seatomatic.paper.model.InvigilatorAssignment;
import com.seatomatic.paper.model.PaperDistribution;
import com.seatomatic.paper.model.QuestionPaper;
import com.seatomatic.paper.model.dto.PacketSlipDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Full lifecycle end-to-end integration test exercising all Module 4 features.
 */
class Module4EndToEndIntegrationTest {

    private QuestionPaperDao mockPaperDao;
    private PaperDistributionDao mockDistDao;
    private InvigilatorAssignmentDao mockAssignDao;
    private DiscrepancyIncidentDao mockIncidentDao;
    private AuditPublisher mockAudit;

    private PaperService paperService;
    private DistributionWorkflowService workflowService;
    private InvigilationService invigilationService;
    private DiscrepancyIncidentService incidentService;
    private PacketSlipGenerator slipGenerator;

    @BeforeEach
    void setUp() {
        mockPaperDao = mock(QuestionPaperDao.class);
        mockDistDao = mock(PaperDistributionDao.class);
        mockAssignDao = mock(InvigilatorAssignmentDao.class);
        mockIncidentDao = mock(DiscrepancyIncidentDao.class);
        mockAudit = mock(AuditPublisher.class);

        paperService = new PaperService(mockPaperDao, mockDistDao, new PaperRequirementCalculator(), mockAudit);
        workflowService = new DistributionWorkflowService(mockDistDao, mockPaperDao, mockAudit);
        invigilationService = new InvigilationService(
                mockAssignDao, mockDistDao, mockPaperDao,
                new PaperRequirementCalculator(), workflowService, mockAudit
        );
        incidentService = new DiscrepancyIncidentService(mockIncidentDao, mockAudit);
        slipGenerator = new PacketSlipGenerator();
    }

    @Test
    @DisplayName("E2E Lifecycle: Creation -> Calculation -> Slips -> Dispatch -> Attendance -> Reconciliation")
    void testCompleteModule4LifecyclePipeline() throws Exception {
        Long examId = 1L;
        Long subjectId = 10L;
        Long roomId = 101L;
        Long invigilatorId = 77L;
        Long distId = 500L;

        // 1. Paper Stock Management
        QuestionPaper paperA = new QuestionPaper(examId, subjectId, "Set A", 150);
        paperA.setId(1001L);
        when(mockPaperDao.insert(any(QuestionPaper.class))).thenReturn(1001L);
        when(mockPaperDao.findById(1001L)).thenReturn(Optional.of(paperA));

        QuestionPaper created = paperService.createPaperSet(examId, subjectId, "Set A", 150, 1L);
        assertNotNull(created);
        assertEquals(150, created.getTotalCopies());

        // 2. Packet Requirement Setup (40 students + 2 buffer = 42 total)
        PaperDistribution dist = new PaperDistribution(5L, roomId, 1001L, 42, 2);
        dist.setId(distId);
        dist.setExamId(examId);
        dist.setRoomId(roomId);
        dist.setRoomCode("LH-1");
        dist.setSubjectId(subjectId);
        dist.setSubjectCode("CS101");
        dist.setSubjectName("Computer Science");
        dist.setSetCode("Set A");
        dist.setStatus(DistributionStatus.PENDING);

        when(mockDistDao.findById(distId)).thenReturn(Optional.of(dist));

        // 3. Envelope Slip Generation
        PacketSlipDTO slip = slipGenerator.generateSlip(dist, "Annual Exam 2026", "2026-10-30", "Prof. Turing");
        assertEquals(40, slip.getStudentCount());
        assertEquals(2, slip.getBufferCount());
        assertEquals(42, slip.getTotalCopies());

        // 4. Packet Preparation (PENDING -> PREPARED)
        when(mockDistDao.updateStatus(distId, DistributionStatus.PREPARED)).thenReturn(1);
        workflowService.markPrepared(distId, 1L);
        assertEquals(DistributionStatus.PREPARED, dist.getStatus());

        // 5. Packet Dispatch to Exam Hall (PREPARED -> DISTRIBUTED)
        when(mockDistDao.updateDistributed(distId, 42, DistributionStatus.DISTRIBUTED)).thenReturn(1);
        workflowService.markDistributed(distId, 42, 1L);
        assertEquals(DistributionStatus.DISTRIBUTED, dist.getStatus());
        assertEquals(42, dist.getDistributed());

        // 6. Invigilator Room Check-in & Security Verification
        when(mockAssignDao.isAssigned(examId, roomId, invigilatorId)).thenReturn(true);
        when(mockAssignDao.recordCheckIn(examId, roomId, invigilatorId)).thenReturn(1);

        boolean checkedIn = invigilationService.recordCheckIn(examId, roomId, invigilatorId, Role.INVIGILATOR);
        assertTrue(checkedIn);

        // 7. Handled Incident Reporting (1 damaged paper detected during exam)
        when(mockIncidentDao.insert(any(DiscrepancyIncident.class))).thenReturn(901L);
        DiscrepancyIncident incident = incidentService.reportIncident(
                distId, examId, roomId, "DAMAGED_COPIES", 1,
                "One paper copy was misprinted; replaced from buffer copies", invigilatorId
        );
        assertEquals(901L, incident.getId());

        // 8. Script Collection and Terminal Reconciliation (DISTRIBUTED -> VERIFIED)
        when(mockDistDao.updateCollected(distId, 42, DistributionStatus.VERIFIED)).thenReturn(1);
        invigilationService.submitRoomCollection(examId, roomId, distId, 42, invigilatorId, Role.INVIGILATOR);
        assertEquals(DistributionStatus.VERIFIED, dist.getStatus());
        assertEquals(42, dist.getCollected());

        // 9. Verify Comprehensive Audit Trail Dispatched
        verify(mockAudit).publish(eq("CREATE_PAPER_SET"), anyString(), eq("QUESTION_PAPER"), anyLong(), anyString());
        verify(mockAudit).publish(eq("PREPARE_PACKET"), anyString(), eq("PAPER_DISTRIBUTION"), anyLong(), anyString());
        verify(mockAudit).publish(eq("DISPATCH_PAPERS"), anyString(), eq("PAPER_DISTRIBUTION"), anyLong(), anyString());
        verify(mockAudit).publish(eq("INVIGILATOR_CHECK_IN"), anyString(), eq("EXAM_ROOM"), anyLong(), anyString());
        verify(mockAudit).publish(eq("EXAM_DISCREPANCY_REPORTED"), anyString(), eq("EXAM_DISCREPANCY"), anyLong(), anyString());
        verify(mockAudit).publish(eq("VERIFY_RECONCILIATION"), anyString(), eq("PAPER_DISTRIBUTION"), anyLong(), anyString());
    }
}
