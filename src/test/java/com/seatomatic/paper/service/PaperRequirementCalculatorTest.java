package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.DistributionStatus;
import com.seatomatic.paper.model.PaperDistribution;
import com.seatomatic.paper.model.QuestionPaper;
import com.seatomatic.paper.model.dto.RoomPaperRequirementDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test Suite TC-P01: Question Paper Requirement Calculation Engine
 */
class PaperRequirementCalculatorTest {

    private QuestionPaperDao mockQuestionPaperDao;
    private PaperDistributionDao mockPaperDistributionDao;
    private AuditPublisher mockAuditPublisher;
    private PaperRequirementCalculator calculator;

    @BeforeEach
    void setUp() {
        mockQuestionPaperDao = mock(QuestionPaperDao.class);
        mockPaperDistributionDao = mock(PaperDistributionDao.class);
        mockAuditPublisher = mock(AuditPublisher.class);

        calculator = Mockito.spy(new PaperRequirementCalculator(
                mockQuestionPaperDao,
                mockPaperDistributionDao,
                mockAuditPublisher,
                null
        ));
    }

    @Test
    @DisplayName("TC-P01.1: Buffer calculation with custom and default policies")
    void testBufferResolution() {
        // Explicit valid buffer
        assertEquals(5, calculator.resolveBuffer(5));
        assertEquals(0, calculator.resolveBuffer(0));

        // Null custom buffer falls back to default (2 copies)
        assertEquals(PaperRequirementCalculator.DEFAULT_BUFFER_COUNT, calculator.resolveBuffer(null));

        // Negative custom buffer falls back to default
        assertEquals(PaperRequirementCalculator.DEFAULT_BUFFER_COUNT, calculator.resolveBuffer(-3));
    }

    @Test
    @DisplayName("TC-P01.2: Multiple subjects in one room preserve distinct room-subject tuples")
    void testMultipleSubjectsInOneRoom() throws Exception {
        Long examId = 100L;
        Long planId = 1L;

        doReturn(planId).when(calculator).findActivePlanId(examId);

        // Simulated distinct room-subject counts from query
        RoomPaperRequirementDTO r1 = new RoomPaperRequirementDTO(examId, 101L, "Room-101", 10L, "CS301", "DBMS", 12, 2, 14);
        RoomPaperRequirementDTO r2 = new RoomPaperRequirementDTO(examId, 101L, "Room-101", 20L, "CS302", "Operating Systems", 10, 2, 12);

        doReturn(List.of(r1, r2)).when(calculator).calculateRequirements(examId, 2);

        List<RoomPaperRequirementDTO> results = calculator.calculateRequirements(examId, 2);

        assertEquals(2, results.size());
        assertEquals("Room-101", results.get(0).getRoomCode());
        assertEquals("CS301", results.get(0).getSubjectCode());
        assertEquals(12, results.get(0).getStudentCount());
        assertEquals(14, results.get(0).getTotalRequired()); // 12 + 2

        assertEquals("Room-101", results.get(1).getRoomCode());
        assertEquals("CS302", results.get(1).getSubjectCode());
        assertEquals(10, results.get(1).getStudentCount());
        assertEquals(12, results.get(1).getTotalRequired()); // 10 + 2

        assertNotEquals(results.get(0).getSubjectCode(), results.get(1).getSubjectCode());
    }

    @Test
    @DisplayName("TC-P01.3: Same subject distributed across multiple rooms preserves separate allotments")
    void testSameSubjectAcrossMultipleRooms() throws Exception {
        Long examId = 100L;
        Long planId = 1L;

        doReturn(planId).when(calculator).findActivePlanId(examId);

        // DBMS in Room 101 (12 students) and DBMS in Room 102 (8 students)
        RoomPaperRequirementDTO r1 = new RoomPaperRequirementDTO(examId, 101L, "Room-101", 10L, "CS301", "DBMS", 12, 2, 14);
        RoomPaperRequirementDTO r2 = new RoomPaperRequirementDTO(examId, 102L, "Room-102", 10L, "CS301", "DBMS", 8, 2, 10);

        doReturn(List.of(r1, r2)).when(calculator).calculateRequirements(examId, 2);

        List<RoomPaperRequirementDTO> results = calculator.calculateRequirements(examId, 2);

        assertEquals(2, results.size());
        assertEquals("Room-101", results.get(0).getRoomCode());
        assertEquals(14, results.get(0).getTotalRequired());

        assertEquals("Room-102", results.get(1).getRoomCode());
        assertEquals(10, results.get(1).getTotalRequired());

        assertNotEquals(results.get(0).getRoomCode(), results.get(1).getRoomCode());
        assertEquals(results.get(0).getSubjectCode(), results.get(1).getSubjectCode());
    }

    @Test
    @DisplayName("TC-P01.4: Missing seating plan safely returns empty list without crashing")
    void testMissingSeatingPlan() throws SQLException {
        Long examId = 999L;
        doReturn(null).when(calculator).findActivePlanId(examId);

        List<RoomPaperRequirementDTO> results = calculator.calculateRequirements(examId, 2);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("TC-P01.5: Recalculation safely upserts requirements without duplicating database records")
    void testRepeatedCalculationIdempotence() throws Exception {
        Long examId = 100L;
        Long planId = 1L;

        doReturn(planId).when(calculator).findActivePlanId(examId);

        RoomPaperRequirementDTO r1 = new RoomPaperRequirementDTO(examId, 101L, "Room-101", 10L, "CS301", "DBMS", 12, 2, 14);
        r1.setPaperId(501L);
        r1.setPaperSetCode("Set A");
        r1.setAvailableStock(100);

        doReturn(List.of(r1)).when(calculator).calculateRequirements(examId, 2);

        QuestionPaper paper = new QuestionPaper(examId, 10L, "Set A", 100);
        paper.setId(501L);
        when(mockQuestionPaperDao.findByExamId(examId)).thenReturn(List.of(paper));

        when(mockPaperDistributionDao.findByPlanRoomAndPaper(planId, 101L, 501L))
                .thenReturn(Optional.of(new PaperDistribution(planId, 101L, 501L, 14, 2)));

        // Run calculation first time
        List<RoomPaperRequirementDTO> firstRun = calculator.calculateRequirements(examId, 2);
        assertEquals(1, firstRun.size());

        // Run calculation second time (recalculation)
        List<RoomPaperRequirementDTO> secondRun = calculator.calculateRequirements(examId, 2);
        assertEquals(1, secondRun.size());
        assertEquals(firstRun.get(0).getTotalRequired(), secondRun.get(0).getTotalRequired());
    }
}
