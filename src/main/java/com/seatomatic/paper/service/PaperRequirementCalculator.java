package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.common.db.TransactionManager;
import com.seatomatic.exam.contract.RoomQueryService;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.DistributionStatus;
import com.seatomatic.paper.model.PaperDistribution;
import com.seatomatic.paper.model.QuestionPaper;
import com.seatomatic.paper.model.dto.RoomPaperRequirementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Calculation engine for room-wise and subject-wise examination question paper requirements.
 * Enforces formula: Required papers = Assigned students (per room, subject) + Configured buffer.
 * Preserves distinct (room, subject) tuples and avoids flat per-room aggregation.
 */
public class PaperRequirementCalculator extends BaseDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaperRequirementCalculator.class);

    public static final String DEFAULT_BUFFER_PROPERTY = "seatomatic.paper.buffer.default";
    public static final int DEFAULT_BUFFER_COUNT = 2; // Default 2 buffer copies per room-subject

    private final QuestionPaperDao questionPaperDao;
    private final PaperDistributionDao paperDistributionDao;
    private final AuditPublisher auditPublisher;
    private final RoomQueryService roomQueryService;

    public PaperRequirementCalculator() {
        this(new QuestionPaperDao(), new PaperDistributionDao(), null, null);
    }

    public PaperRequirementCalculator(QuestionPaperDao questionPaperDao,
                                      PaperDistributionDao paperDistributionDao,
                                      AuditPublisher auditPublisher,
                                      RoomQueryService roomQueryService) {
        this.questionPaperDao = questionPaperDao != null ? questionPaperDao : new QuestionPaperDao();
        this.paperDistributionDao = paperDistributionDao != null ? paperDistributionDao : new PaperDistributionDao();
        this.auditPublisher = auditPublisher;
        this.roomQueryService = roomQueryService;
    }

    /**
     * Resolves the configured buffer count.
     * Prefers explicit parameter if valid (>= 0), otherwise falls back to system configuration or default.
     */
    public int resolveBuffer(Integer customBuffer) {
        if (customBuffer != null && customBuffer >= 0) {
            return customBuffer;
        }
        String prop = System.getProperty(DEFAULT_BUFFER_PROPERTY);
        if (prop != null && !prop.isBlank()) {
            try {
                int parsed = Integer.parseInt(prop.trim());
                if (parsed >= 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return DEFAULT_BUFFER_COUNT;
    }

    /**
     * Finds the active seating plan ID for an examination.
     * Prioritizes LOCKED or APPROVED plans, or the latest active plan.
     */
    public Long findActivePlanId(Long examId) throws SQLException {
        if (examId == null) {
            return null;
        }
        String sql = "SELECT id FROM seating_plans " +
                     "WHERE exam_id = ? AND status <> 'SUPERSEDED' " +
                     "ORDER BY CASE status " +
                     "    WHEN 'LOCKED' THEN 1 " +
                     "    WHEN 'APPROVED' THEN 2 " +
                     "    WHEN 'REVIEWED' THEN 3 " +
                     "    WHEN 'GENERATED' THEN 4 " +
                     "    ELSE 5 END, id DESC LIMIT 1";
        return queryOne(sql, rs -> rs.getLong(1), examId);
    }

    /**
     * Calculates room-wise, subject-wise paper requirements for an examination.
     * Distinct room and subject pairings are strictly preserved.
     */
    public List<RoomPaperRequirementDTO> calculateRequirements(Long examId, Integer customBuffer) throws SQLException {
        if (examId == null) {
            return Collections.emptyList();
        }
        int buffer = resolveBuffer(customBuffer);
        Long planId = findActivePlanId(examId);
        if (planId == null) {
            LOGGER.warn("No active seating plan found for examination id: {}", examId);
            return Collections.emptyList();
        }

        String sql =
                "SELECT r.id AS room_id, r.room_code, " +
                "       s.id AS subject_id, s.code AS subject_code, s.name AS subject_name, " +
                "       COUNT(DISTINCT sa.student_id) AS student_count " +
                "FROM seating_plans sp " +
                "JOIN seat_assignments sa ON sa.plan_id = sp.id " +
                "JOIN seats st ON sa.seat_id = st.id " +
                "JOIN rooms r ON st.room_id = r.id " +
                "JOIN exam_students es ON es.exam_id = sp.exam_id AND es.student_id = sa.student_id " +
                "JOIN subjects s ON es.subject_id = s.id " +
                "WHERE sp.id = ? " +
                "  AND st.is_blocked = FALSE " +
                "  AND r.active = TRUE " +
                "GROUP BY r.id, r.room_code, s.id, s.code, s.name " +
                "ORDER BY r.room_code ASC, s.code ASC";

        List<RoomPaperRequirementDTO> results = queryList(sql, rs -> {
            RoomPaperRequirementDTO dto = new RoomPaperRequirementDTO();
            dto.setExamId(examId);
            dto.setRoomId(rs.getLong("room_id"));
            dto.setRoomCode(rs.getString("room_code"));
            dto.setSubjectId(rs.getLong("subject_id"));
            dto.setSubjectCode(rs.getString("subject_code"));
            dto.setSubjectName(rs.getString("subject_name"));

            int studentCount = rs.getInt("student_count");
            dto.setStudentCount(studentCount);
            dto.setBufferCount(buffer);
            dto.setTotalRequired(studentCount + buffer);
            return dto;
        }, planId);

        // Populate question paper information (sets and stock) and existing distribution statuses
        for (RoomPaperRequirementDTO dto : results) {
            enrichWithPaperStockAndStatus(planId, dto);
        }

        return results;
    }

    /**
     * Calculates requirements and transactionally creates or refreshes paper_distribution records.
     * Prevents overwriting legitimate distribution progress (e.g., DISTRIBUTED, VERIFIED).
     */
    public List<RoomPaperRequirementDTO> calculateAndPersistRequirements(Long examId, Integer customBuffer, Long actorUserId)
            throws Exception {
        if (examId == null) {
            throw new IllegalArgumentException("Examination ID must not be null");
        }
        Long planId = findActivePlanId(examId);
        if (planId == null) {
            throw new IllegalStateException("Cannot calculate paper requirements: No active seating plan exists for exam " + examId);
        }

        List<RoomPaperRequirementDTO> requirements = calculateRequirements(examId, customBuffer);
        if (requirements.isEmpty()) {
            LOGGER.info("No room-subject allocations found for exam id {}", examId);
            return requirements;
        }

        try (TransactionManager tm = new TransactionManager()) {
            tm.begin();
            for (RoomPaperRequirementDTO req : requirements) {
                // Resolve paperId: if not set, find first available paper set for this exam and subject
                Long paperId = req.getPaperId();
                if (paperId == null) {
                    List<QuestionPaper> papers = questionPaperDao.findByExamId(examId);
                    for (QuestionPaper qp : papers) {
                        if (qp.getSubjectId().equals(req.getSubjectId())) {
                            paperId = qp.getId();
                            req.setPaperId(paperId);
                            req.setPaperSetCode(qp.getSetCode());
                            req.setAvailableStock(qp.getTotalCopies());
                            break;
                        }
                    }
                }

                if (paperId != null) {
                    paperDistributionDao.upsertRequirement(planId, req.getRoomId(), paperId, req.getTotalRequired(), req.getBufferCount());
                    Optional<PaperDistribution> existing = paperDistributionDao.findByPlanRoomAndPaper(planId, req.getRoomId(), paperId);
                    if (existing.isPresent()) {
                        req.setDistributionStatus(existing.get().getStatus());
                        req.setDistributedCount(existing.get().getDistributed());
                        req.setCollectedCount(existing.get().getCollected());
                    }
                }
            }
            tm.commit();
        }

        if (auditPublisher != null && actorUserId != null) {
            auditPublisher.publish("CALCULATE_PAPER_REQUIREMENTS",
                    String.valueOf(actorUserId),
                    "EXAM",
                    examId,
                    String.format("{\"calculatedTuples\":%d,\"buffer\":%d,\"planId\":%d}",
                            requirements.size(), resolveBuffer(customBuffer), planId));
        }

        return requirements;
    }

    private void enrichWithPaperStockAndStatus(Long planId, RoomPaperRequirementDTO dto) throws SQLException {
        // Find existing paper set
        List<QuestionPaper> papers = questionPaperDao.findByExamId(dto.getExamId());
        for (QuestionPaper qp : papers) {
            if (qp.getSubjectId().equals(dto.getSubjectId())) {
                dto.setPaperId(qp.getId());
                dto.setPaperSetCode(qp.getSetCode());
                dto.setAvailableStock(qp.getTotalCopies());
                break;
            }
        }

        if (dto.getPaperId() != null) {
            Optional<PaperDistribution> distOpt = paperDistributionDao.findByPlanRoomAndPaper(planId, dto.getRoomId(), dto.getPaperId());
            if (distOpt.isPresent()) {
                PaperDistribution dist = distOpt.get();
                dto.setDistributionStatus(dist.getStatus());
                dto.setDistributedCount(dist.getDistributed());
                dto.setCollectedCount(dist.getCollected());
            } else {
                dto.setDistributionStatus(DistributionStatus.PENDING);
            }
        }
    }

    /**
     * Synchronizes existing paper distribution records with the latest active seating plan,
     * pruning obsolete PENDING room packets if rooms have been de-allocated or removed.
     */
    public int syncDistributionWithActivePlan(Long examId, Long actorUserId) throws Exception {
        if (examId == null) {
            return 0;
        }
        Long planId = findActivePlanId(examId);
        if (planId == null) {
            return 0;
        }
        List<RoomPaperRequirementDTO> current = calculateRequirements(examId, null);
        List<Long> activeRoomIds = current.stream()
                .map(RoomPaperRequirementDTO::getRoomId)
                .distinct()
                .toList();

        int pruned = paperDistributionDao.deleteOrphanedPendingRecords(planId, activeRoomIds);
        if (pruned > 0 && auditPublisher != null && actorUserId != null) {
            auditPublisher.publish("SYNC_DISTRIBUTION_PLAN", String.valueOf(actorUserId),
                    "EXAM", examId,
                    String.format("{\"planId\":%d,\"prunedOrphans\":%d}", planId, pruned));
        }
        return pruned;
    }
}
