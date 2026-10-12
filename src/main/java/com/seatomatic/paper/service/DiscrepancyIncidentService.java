package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.paper.dao.DiscrepancyIncidentDao;
import com.seatomatic.paper.model.DiscrepancyIncident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing examination discrepancies, damaged papers, and collection deficits.
 * Automatically publishes high-priority audit trail events.
 */
public class DiscrepancyIncidentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscrepancyIncidentService.class);

    private final DiscrepancyIncidentDao incidentDao;
    private final AuditPublisher auditPublisher;

    public DiscrepancyIncidentService() {
        this(new DiscrepancyIncidentDao(), null);
    }

    public DiscrepancyIncidentService(DiscrepancyIncidentDao incidentDao, AuditPublisher auditPublisher) {
        this.incidentDao = incidentDao != null ? incidentDao : new DiscrepancyIncidentDao();
        this.auditPublisher = auditPublisher;
    }

    public DiscrepancyIncident reportIncident(Long distributionId, Long examId, Long roomId,
                                              String incidentType, int countAffected,
                                              String description, Long reporterUserId) throws Exception {
        if (examId == null || roomId == null) {
            throw new ValidationException("Examination ID and Room ID are required for incident logging.");
        }
        if (incidentType == null || incidentType.isBlank()) {
            throw new ValidationException("Incident type must be specified.");
        }
        if (countAffected < 0) {
            throw new ValidationException("Count affected cannot be negative.");
        }
        if (reporterUserId == null) {
            throw new ValidationException("Reporter user ID is required.");
        }

        DiscrepancyIncident incident = new DiscrepancyIncident(
                distributionId, examId, roomId, incidentType.trim().toUpperCase(),
                countAffected, description != null ? description.trim() : "", reporterUserId
        );

        Long id = incidentDao.insert(incident);
        incident.setId(id);

        LOGGER.warn("Exam incident reported: id={} examId={} roomId={} type={} affected={} reporter={}",
                id, examId, roomId, incident.getIncidentType(), countAffected, reporterUserId);

        if (auditPublisher != null) {
            auditPublisher.publish("EXAM_DISCREPANCY_REPORTED", String.valueOf(reporterUserId),
                    "EXAM_DISCREPANCY", id,
                    String.format("{\"examId\":%d,\"roomId\":%d,\"type\":\"%s\",\"affected\":%d}",
                            examId, roomId, incident.getIncidentType(), countAffected));
        }

        return incident;
    }

    public void resolveIncident(Long incidentId, Long resolverUserId, String resolutionNotes) throws Exception {
        if (incidentId == null) {
            throw new ValidationException("Incident ID is required.");
        }
        Optional<DiscrepancyIncident> opt = incidentDao.findById(incidentId);
        if (opt.isEmpty()) {
            throw new ValidationException("Incident not found for ID: " + incidentId);
        }

        incidentDao.updateStatus(incidentId, "RESOLVED");

        if (auditPublisher != null && resolverUserId != null) {
            auditPublisher.publish("EXAM_DISCREPANCY_RESOLVED", String.valueOf(resolverUserId),
                    "EXAM_DISCREPANCY", incidentId,
                    String.format("{\"resolverUserId\":%d,\"notes\":\"%s\"}",
                            resolverUserId, resolutionNotes != null ? resolutionNotes : ""));
        }
    }

    public List<DiscrepancyIncident> getIncidentsForExam(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        return incidentDao.findByExamId(examId);
    }

    public List<DiscrepancyIncident> getIncidentsForRoom(Long roomId) throws SQLException {
        if (roomId == null) {
            return List.of();
        }
        return incidentDao.findByRoomId(roomId);
    }

    public Optional<DiscrepancyIncident> getIncidentById(Long id) throws SQLException {
        if (id == null) {
            return Optional.empty();
        }
        return incidentDao.findById(id);
    }
}
