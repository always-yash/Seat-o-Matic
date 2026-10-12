package com.seatomatic.paper.controller;

import com.seatomatic.common.exception.SecurityException;
import com.seatomatic.common.security.Role;
import com.seatomatic.paper.model.dto.InvigilatorRoomViewDTO;
import com.seatomatic.paper.service.DistributionWorkflowService;
import com.seatomatic.paper.service.InvigilationService;
import com.seatomatic.paper.service.PaperRequirementCalculator;
import com.seatomatic.paper.service.PaperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Controller-level test suite for Paper, Distribution, and Invigilator servlets.
 */
class PaperControllerTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(response.encodeRedirectURL(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("InvigilatorServlet GET forwards to 403 on IDOR SecurityException")
    void testInvigilatorServletGetForbiddenOnIdor() throws Exception {
        InvigilationService mockInvigService = mock(InvigilationService.class);
        InvigilatorServlet servlet = new InvigilatorServlet(mockInvigService);

        when(session.getAttribute("userId")).thenReturn(77L);
        when(session.getAttribute("userRole")).thenReturn("INVIGILATOR");
        when(request.getParameter("examId")).thenReturn("100");
        when(request.getParameter("roomId")).thenReturn("102");

        // InvigilationService throws SecurityException because invigilator 77 is not assigned to room 102
        doThrow(new SecurityException("Access Denied: You are not authorized."))
                .when(mockInvigService).getRoomView(100L, 102L, 77L, Role.INVIGILATOR);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/error/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("InvigilatorServlet GET renders view when authorized")
    void testInvigilatorServletGetRendersWhenAuthorized() throws Exception {
        InvigilationService mockInvigService = mock(InvigilationService.class);
        InvigilatorServlet servlet = new InvigilatorServlet(mockInvigService);

        when(session.getAttribute("userId")).thenReturn(77L);
        when(session.getAttribute("userRole")).thenReturn("INVIGILATOR");
        when(request.getParameter("examId")).thenReturn("100");
        when(request.getParameter("roomId")).thenReturn("101");

        InvigilatorRoomViewDTO viewDTO = new InvigilatorRoomViewDTO();
        viewDTO.setExamId(100L);
        viewDTO.setRoomId(101L);
        viewDTO.setRoomCode("Room-101");

        when(mockInvigService.getRoomView(100L, 101L, 77L, Role.INVIGILATOR)).thenReturn(viewDTO);

        servlet.doGet(request, response);

        verify(request).setAttribute("roomView", viewDTO);
        verify(request).getRequestDispatcher("/WEB-INF/views/invigilator/room.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("DistributionServlet calculate action invokes calculator")
    void testDistributionServletCalculate() throws Exception {
        PaperRequirementCalculator mockCalc = mock(PaperRequirementCalculator.class);
        DistributionWorkflowService mockWorkflow = mock(DistributionWorkflowService.class);
        CommonLookupDao mockLookup = mock(CommonLookupDao.class);

        DistributionServlet servlet = new DistributionServlet(mockCalc, mockWorkflow, mockLookup);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(request.getParameter("action")).thenReturn("calculate");
        when(request.getParameter("examId")).thenReturn("100");
        when(request.getParameter("buffer")).thenReturn("3");

        servlet.doPost(request, response);

        verify(mockCalc).calculateAndPersistRequirements(100L, 3, 1L);
        verify(response).sendRedirect("/distribution?examId=100&buffer=3");
    }

    @Test
    @DisplayName("PaperServlet create action invokes paperService")
    void testPaperServletCreate() throws Exception {
        PaperService mockPaperService = mock(PaperService.class);
        CommonLookupDao mockLookup = mock(CommonLookupDao.class);
        PaperServlet servlet = new PaperServlet(mockPaperService, mockLookup);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("examId")).thenReturn("100");
        when(request.getParameter("subjectId")).thenReturn("10");
        when(request.getParameter("setCode")).thenReturn("Set A");
        when(request.getParameter("totalCopies")).thenReturn("150");

        com.seatomatic.paper.model.QuestionPaper created = new com.seatomatic.paper.model.QuestionPaper(100L, 10L, "SET A", 150);
        created.setId(99L);
        when(mockPaperService.createPaperSet(100L, 10L, "Set A", 150, 1L)).thenReturn(created);

        servlet.doPost(request, response);

        verify(mockPaperService).createPaperSet(100L, 10L, "Set A", 150, 1L);
        verify(response).sendRedirect("/papers?examId=100");
    }
}
