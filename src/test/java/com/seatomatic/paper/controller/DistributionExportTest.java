package com.seatomatic.paper.controller;

import com.seatomatic.paper.model.DistributionStatus;
import com.seatomatic.paper.model.dto.RoomPaperRequirementDTO;
import com.seatomatic.paper.service.DistributionWorkflowService;
import com.seatomatic.paper.service.PaperRequirementCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test suite verifying CSV distribution manifest streaming and header formatting.
 */
class DistributionExportTest {

    private PaperRequirementCalculator mockCalculator;
    private DistributionWorkflowService mockWorkflowService;
    private CommonLookupDao mockLookupDao;
    private DistributionServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        mockCalculator = mock(PaperRequirementCalculator.class);
        mockWorkflowService = mock(DistributionWorkflowService.class);
        mockLookupDao = mock(CommonLookupDao.class);
        servlet = new DistributionServlet(mockCalculator, mockWorkflowService, mockLookupDao);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("CSV Export: Streams valid CSV headers and escaped distribution data")
    void testExportCsvStreamingAndHeaders() throws Exception {
        Long examId = 100L;
        when(request.getParameter("action")).thenReturn("export");
        when(request.getParameter("examId")).thenReturn("100");

        RoomPaperRequirementDTO req = new RoomPaperRequirementDTO();
        req.setExamId(examId);
        req.setRoomId(101L);
        req.setRoomCode("LH-101, Ground Floor");
        req.setSubjectId(10L);
        req.setSubjectCode("CS101");
        req.setSubjectName("Intro to Computer Science");
        req.setPaperSetCode("Set A");
        req.setStudentCount(40);
        req.setBufferCount(5);
        req.setTotalRequired(45);
        req.setDistributedCount(45);
        req.setCollectedCount(45);
        req.setDistributionStatus(DistributionStatus.VERIFIED);

        when(mockCalculator.calculateRequirements(eq(examId), anyInt())).thenReturn(List.of(req));

        servlet.doGet(request, response);

        verify(response).setContentType("text/csv; charset=UTF-8");
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"distribution-manifest-exam-100.csv\"");

        String csvOutput = responseWriter.toString();
        assertNotNull(csvOutput);
        assertTrue(csvOutput.startsWith("RoomCode,SubjectCode,SubjectName,PaperSet,StudentCount,BufferCount,TotalRequired,Distributed,Collected,Status"));
        assertTrue(csvOutput.contains("\"LH-101, Ground Floor\""));
        assertTrue(csvOutput.contains("\"CS101\""));
        assertTrue(csvOutput.contains("\"Set A\""));
        assertTrue(csvOutput.contains("40,5,45,45,45,\"VERIFIED\""));
    }

    @Test
    @DisplayName("CSV Export: Responds with HTTP 400 Bad Request when exam ID is missing")
    void testExportMissingExamIdReturnsBadRequest() throws Exception {
        when(request.getParameter("action")).thenReturn("export");
        when(request.getParameter("examId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("Examination ID is required"));
    }
}
