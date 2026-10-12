package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.db.TransactionManager;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.DistributionStatus;
import com.seatomatic.paper.model.PaperDistribution;
import com.seatomatic.paper.model.QuestionPaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service enforcing the guarded state machine lifecycle:
 * PENDING -> PREPARED -> DISTRIBUTED -> VERIFIED (Terminal).
 * All state modifications are persisted transactionally and audited.
 */
public class DistributionWorkflowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionWorkflowService.class);

    private final PaperDistributionDao paperDistributionDao;
    private final QuestionPaperDao questionPaperDao;
    private final AuditPublisher auditPublisher;

    public DistributionWorkflowService() {
        this(new PaperDistributionDao(), new QuestionPaperDao(), null);
    }

    public DistributionWorkflowService(PaperDistributionDao paperDistributionDao,
                                       QuestionPaperDao questionPaperDao,
                                       AuditPublisher auditPublisher) {
        this.paperDistributionDao = paperDistributionDao != null ? paperDistributionDao : new PaperDistributionDao();
        this.questionPaperDao = questionPaperDao != null ? questionPaperDao : new QuestionPaperDao();
        this.auditPublisher = auditPublisher;
    }

    /**
     * Transitions a distribution packet from PENDING to PREPARED.
     * Validates that stock is sufficient.
     */
    public void markPrepared(Long distributionId, Long actorUserId) throws Exception {
        PaperDistribution dist = loadDistribution(distributionId);

        if (!dist.getStatus().canTransitionTo(DistributionStatus.PREPARED)) {
            throw new ValidationException(String.format(
                    "Illegal state transition: Cannot transition from %s to PREPARED. (Only PENDING can be prepared)",
                    dist.getStatus()));
        }

        // Verify printed stock sufficiency
        Optional<QuestionPaper> paperOpt = questionPaperDao.findById(dist.getPaperId());
        if (paperOpt.isPresent()) {
            QuestionPaper paper = paperOpt.get();
            if (paper.getTotalCopies() < dist.getRequired()) {
                throw new ValidationException(String.format(
                        "Stock shortfall for Set '%s': Required %d copies, but available printed stock is %d.",
                        paper.getSetCode(), dist.getRequired(), paper.getTotalCopies()));
            }
        }

        try (TransactionManager tm = new TransactionManager()) {
            tm.begin();
            paperDistributionDao.updateStatus(dist.getId(), DistributionStatus.PREPARED);
            tm.commit();
        }

        dist.setStatus(DistributionStatus.PREPARED);
        logAudit("PREPARE_PACKET", actorUserId, dist.getId(),
                String.format("{\"required\":%d,\"paperId\":%d,\"status\":\"PREPARED\"}", dist.getRequired(), dist.getPaperId()));
    }

    /**
     * Transitions a distribution packet from PREPARED to DISTRIBUTED.
     * Validates that the distributed count is valid.
     */
    public void markDistributed(Long distributionId, int distributedCount, Long actorUserId) throws Exception {
        PaperDistribution dist = loadDistribution(distributionId);

        if (!dist.getStatus().canTransitionTo(DistributionStatus.DISTRIBUTED)) {
            throw new ValidationException(String.format(
                    "Illegal state transition: Cannot transition from %s to DISTRIBUTED. (Packets must be PREPARED first)",
                    dist.getStatus()));
        }

        if (distributedCount <= 0) {
            throw new ValidationException("Distributed count must be greater than zero.");
        }

        // Stock check
        Optional<QuestionPaper> paperOpt = questionPaperDao.findById(dist.getPaperId());
        if (paperOpt.isPresent() && paperOpt.get().getTotalCopies() < distributedCount) {
            throw new ValidationException(String.format(
                    "Cannot distribute %d copies: Available stock is only %d.",
                    distributedCount, paperOpt.get().getTotalCopies()));
        }

        try (TransactionManager tm = new TransactionManager()) {
            tm.begin();
            paperDistributionDao.updateDistributed(dist.getId(), distributedCount, DistributionStatus.DISTRIBUTED);
            tm.commit();
        }

        dist.setDistributed(distributedCount);
        dist.setStatus(DistributionStatus.DISTRIBUTED);
        logAudit("DISPATCH_PAPERS", actorUserId, dist.getId(),
                String.format("{\"distributed\":%d,\"required\":%d,\"status\":\"DISTRIBUTED\"}", distributedCount, dist.getRequired()));
    }

    /**
     * Transitions a distribution packet from DISTRIBUTED to VERIFIED.
     * Enforces strict reconciliation between distributed and collected quantities.
     */
    public void recordCollectionAndVerify(Long distributionId, int collectedCount, Long actorUserId) throws Exception {
        PaperDistribution dist = loadDistribution(distributionId);

        if (dist.getStatus() == DistributionStatus.VERIFIED) {
            throw new ValidationException("Distribution record is already VERIFIED. VERIFIED is a terminal state.");
        }

        if (!dist.getStatus().canTransitionTo(DistributionStatus.VERIFIED)) {
            throw new ValidationException(String.format(
                    "Illegal state transition: Cannot transition from %s to VERIFIED. (Must be DISTRIBUTED first)",
                    dist.getStatus()));
        }

        if (collectedCount < 0) {
            throw new ValidationException("Collected count cannot be negative.");
        }

        // Reconciliation rule: collected count must match distributed count
        if (collectedCount != dist.getDistributed()) {
            throw new ValidationException(String.format(
                    "Reconciliation failure: Collected count (%d) does not match distributed count (%d). Discrepancy of %d scripts.",
                    collectedCount, dist.getDistributed(), Math.abs(collectedCount - dist.getDistributed())));
        }

        try (TransactionManager tm = new TransactionManager()) {
            tm.begin();
            paperDistributionDao.updateCollected(dist.getId(), collectedCount, DistributionStatus.VERIFIED);
            tm.commit();
        }

        dist.setCollected(collectedCount);
        dist.setStatus(DistributionStatus.VERIFIED);
        logAudit("VERIFY_RECONCILIATION", actorUserId, dist.getId(),
                String.format("{\"distributed\":%d,\"collected\":%d,\"reconciled\":true,\"status\":\"VERIFIED\"}",
                        dist.getDistributed(), collectedCount));
    }

    /**
     * Prepares all PENDING distribution packets for an exam in a single transaction.
     */
    public int prepareAllPending(Long examId, Long actorUserId) throws Exception {
        if (examId == null) {
            return 0;
        }
        List<PaperDistribution> list = paperDistributionDao.findByExamId(examId);
        int preparedCount = 0;
        try (TransactionManager tm = new TransactionManager()) {
            tm.begin();
            for (PaperDistribution d : list) {
                if (d.getStatus() == DistributionStatus.PENDING) {
                    paperDistributionDao.updateStatus(d.getId(), DistributionStatus.PREPARED);
                    preparedCount++;
                }
            }
            tm.commit();
        }
        if (preparedCount > 0) {
            logAudit("BATCH_PREPARE_PACKETS", actorUserId, examId,
                    String.format("{\"preparedCount\":%d}", preparedCount));
        }
        return preparedCount;
    }

    public List<PaperDistribution> getDistributionByExam(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        return paperDistributionDao.findByExamId(examId);
    }

    public List<PaperDistribution> getDistributionByPlanAndRoom(Long planId, Long roomId) throws SQLException {
        if (planId == null || roomId == null) {
            return List.of();
        }
        return paperDistributionDao.findByPlanAndRoom(planId, roomId);
    }

    public Optional<PaperDistribution> getDistributionById(Long id) throws SQLException {
        if (id == null) {
            return Optional.empty();
        }
        return paperDistributionDao.findById(id);
    }

    private PaperDistribution loadDistribution(Long id) throws SQLException, ValidationException {
        if (id == null) {
            throw new ValidationException("Distribution ID cannot be null.");
        }
        Optional<PaperDistribution> opt = paperDistributionDao.findById(id);
        if (opt.isEmpty()) {
            throw new ValidationException("Paper distribution record not found for ID: " + id);
        }
        return opt.get();
    }

    private void logAudit(String action, Long actorUserId, Long targetId, String details) {
        if (auditPublisher != null && actorUserId != null) {
            auditPublisher.publish(action, String.valueOf(actorUserId), "PAPER_DISTRIBUTION", targetId, details);
        }
    }
}
