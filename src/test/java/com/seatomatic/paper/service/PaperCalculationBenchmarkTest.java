package com.seatomatic.paper.service;

import com.seatomatic.common.db.DBConnection;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.QuestionPaper;
import com.seatomatic.paper.model.dto.RoomPaperRequirementDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Performance benchmark verifying that room-subject paper requirement calculation
 * executes efficiently under high volume (Directive Section 6).
 */
class PaperCalculationBenchmarkTest {

    private DataSource mockDataSource;
    private Connection mockConnection;
    private PreparedStatement mockPsPlan;
    private PreparedStatement mockPsRooms;
    private ResultSet mockRsPlan;
    private ResultSet mockRsRooms;

    private QuestionPaperDao mockPaperDao;
    private PaperDistributionDao mockDistDao;
    private PaperRequirementCalculator calculator;

    @BeforeEach
    void setUp() throws SQLException {
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        mockPsPlan = mock(PreparedStatement.class);
        mockPsRooms = mock(PreparedStatement.class);
        mockRsPlan = mock(ResultSet.class);
        mockRsRooms = mock(ResultSet.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        DBConnection.setDataSourceForTesting(mockDataSource);

        when(mockConnection.prepareStatement(contains("seating_plans"))).thenReturn(mockPsPlan);
        when(mockPsPlan.executeQuery()).thenReturn(mockRsPlan);
        when(mockRsPlan.next()).thenReturn(true, false);
        when(mockRsPlan.getLong(1)).thenReturn(10L);

        when(mockConnection.prepareStatement(contains("COUNT(DISTINCT sa.student_id)"))).thenReturn(mockPsRooms);
        when(mockPsRooms.executeQuery()).thenReturn(mockRsRooms);

        mockPaperDao = mock(QuestionPaperDao.class);
        mockDistDao = mock(PaperDistributionDao.class);

        calculator = new PaperRequirementCalculator(mockPaperDao, mockDistDao, null, null);
    }

    @AfterEach
    void tearDown() {
        DBConnection.resetDataSourceForTesting();
    }

    @Test
    @DisplayName("Performance: 50 rooms high-volume requirement calculation completes in under 250ms")
    void testHighVolumeCalculationThroughput() throws Exception {
        Long examId = 100L;

        // Set up 50 room-subject allocation rows
        int rowCount = 50;
        Boolean[] nextResponses = new Boolean[rowCount + 1];
        Arrays.fill(nextResponses, 0, rowCount, true);
        nextResponses[rowCount] = false;
        when(mockRsRooms.next()).thenReturn(nextResponses[0], Arrays.copyOfRange(nextResponses, 1, nextResponses.length));

        Long[] roomIds = new Long[rowCount];
        String[] roomCodes = new String[rowCount];
        Long[] subjectIds = new Long[rowCount];
        String[] subjectCodes = new String[rowCount];
        String[] subjectNames = new String[rowCount];
        Integer[] studentCounts = new Integer[rowCount];

        List<QuestionPaper> papers = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            long rId = 101L + i;
            long sId = (i % 5) + 1L;
            roomIds[i] = rId;
            roomCodes[i] = "HALL-" + rId;
            subjectIds[i] = sId;
            subjectCodes[i] = "CS10" + sId;
            subjectNames[i] = "Subject " + sId;
            studentCounts[i] = 40 + (i % 10);

            QuestionPaper qp = new QuestionPaper(examId, sId, "Set A", 1000);
            qp.setId(500L + sId);
            papers.add(qp);
        }

        when(mockRsRooms.getLong("room_id")).thenReturn(roomIds[0], Arrays.copyOfRange(roomIds, 1, roomIds.length));
        when(mockRsRooms.getString("room_code")).thenReturn(roomCodes[0], Arrays.copyOfRange(roomCodes, 1, roomCodes.length));
        when(mockRsRooms.getLong("subject_id")).thenReturn(subjectIds[0], Arrays.copyOfRange(subjectIds, 1, subjectIds.length));
        when(mockRsRooms.getString("subject_code")).thenReturn(subjectCodes[0], Arrays.copyOfRange(subjectCodes, 1, subjectCodes.length));
        when(mockRsRooms.getString("subject_name")).thenReturn(subjectNames[0], Arrays.copyOfRange(subjectNames, 1, subjectNames.length));
        when(mockRsRooms.getInt("student_count")).thenReturn(studentCounts[0], Arrays.copyOfRange(studentCounts, 1, studentCounts.length));

        when(mockPaperDao.findByExamId(examId)).thenReturn(papers);

        long startTime = System.nanoTime();
        List<RoomPaperRequirementDTO> results = calculator.calculateRequirements(examId, 5);
        long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000;

        assertNotNull(results);
        assertEquals(50, results.size(), "Should calculate exactly 50 distinct room-subject allocations");
        assertTrue(elapsedMillis < 250, "Calculation must execute in under 250ms (actual: " + elapsedMillis + "ms)");

        for (int i = 0; i < results.size(); i++) {
            RoomPaperRequirementDTO dto = results.get(i);
            assertEquals(5, dto.getBufferCount());
            assertEquals(dto.getStudentCount() + 5, dto.getTotalRequired());
        }
    }
}
