package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.paper.dao.DiscrepancyIncidentDao;
import com.seatomatic.paper.model.DiscrepancyIncident;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite verifying discrepancy and incident reporting functionality.
 */
class DiscrepancyIncidentServiceTest {

    private DiscrepancyIncidentDao mockDao;
    private AuditPublisher mockAudit;
    private DiscrepancyIncidentService service;

    @BeforeEach
    void setUp() {
        mockDao = mock(DiscrepancyIncidentDao.class);
        mockAudit = mock(AuditPublisher.class);
        service = new DiscrepancyIncidentService(mockDao, mockAudit);
    }

    @Test
    @DisplayName("Incident: Reports paper shortfall and publishes security audit event")
    void testReportIncidentSuccess() throws Exception {
        when(mockDao.insert(any(DiscrepancyIncident.class))).thenReturn(501L);

        DiscrepancyIncident incident = service.reportIncident(
                10L, 100L, 201L, "SHORTFALL", 3,
                "Envelope missing 3 copies of Set B", 77L
        );

        assertNotNull(incident);
        assertEquals(501L, incident.getId());
        assertEquals("SHORTFALL", incident.getIncidentType());
        assertEquals(3, incident.getCountAffected());
        assertEquals("REPORTED", incident.getStatus());

        verify(mockDao).insert(any(DiscrepancyIncident.class));
        verify(mockAudit).publish(eq("EXAM_DISCREPANCY_REPORTED"), eq("77"), eq("EXAM_DISCREPANCY"), eq(501L), anyString());
    }

    @Test
    @DisplayName("Incident: Rejects invalid parameters such as null exam or negative count")
    void testReportIncidentValidationRejections() {
        assertThrows(ValidationException.class, () ->
                service.reportIncident(10L, null, 201L, "DAMAGED", 2, "Torn script", 77L));

        assertThrows(ValidationException.class, () ->
                service.reportIncident(10L, 100L, 201L, "", 2, "Torn script", 77L));

        assertThrows(ValidationException.class, () ->
                service.reportIncident(10L, 100L, 201L, "DAMAGED", -5, "Invalid count", 77L));

        assertThrows(ValidationException.class, () ->
                service.reportIncident(10L, 100L, 201L, "DAMAGED", 1, "Valid", null));
    }

    @Test
    @DisplayName("Incident: Resolves incident and emits resolution audit record")
    void testResolveIncident() throws Exception {
        DiscrepancyIncident existing = new DiscrepancyIncident(10L, 100L, 201L, "SHORTFALL", 2, "Resolved", 77L);
        existing.setId(99L);
        when(mockDao.findById(99L)).thenReturn(Optional.of(existing));

        service.resolveIncident(99L, 1L, "Additional buffer packet dispatched to Room 201");

        verify(mockDao).updateStatus(99L, "RESOLVED");
        verify(mockAudit).publish(eq("EXAM_DISCREPANCY_RESOLVED"), eq("1"), eq("EXAM_DISCREPANCY"), eq(99L), anyString());
    }

    @Test
    @DisplayName("Incident: Retrieval queries delegate to DAO")
    void testRetrievalQueries() throws SQLException {
        when(mockDao.findByExamId(100L)).thenReturn(List.of(new DiscrepancyIncident()));
        when(mockDao.findByRoomId(201L)).thenReturn(List.of(new DiscrepancyIncident()));

        assertEquals(1, service.getIncidentsForExam(100L).size());
        assertEquals(1, service.getIncidentsForRoom(201L).size());
    }
}
