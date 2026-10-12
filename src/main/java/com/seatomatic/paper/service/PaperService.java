package com.seatomatic.paper.service;

import com.google.gson.Gson;
import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.paper.contract.PaperQueryService;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.QuestionPaper;
import com.seatomatic.paper.model.dto.RoomPaperRequirementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing question paper sets and implementing the published PaperQueryService contract.
 */
public class PaperService implements PaperQueryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaperService.class);
    private static final Gson GSON = new Gson();

    private final QuestionPaperDao questionPaperDao;
    private final PaperDistributionDao paperDistributionDao;
    private final PaperRequirementCalculator calculator;
    private final AuditPublisher auditPublisher;

    public PaperService() {
        this(new QuestionPaperDao(), new PaperDistributionDao(), new PaperRequirementCalculator(), null);
    }

    public PaperService(QuestionPaperDao questionPaperDao,
                        PaperDistributionDao paperDistributionDao,
                        PaperRequirementCalculator calculator,
                        AuditPublisher auditPublisher) {
        this.questionPaperDao = questionPaperDao != null ? questionPaperDao : new QuestionPaperDao();
        this.paperDistributionDao = paperDistributionDao != null ? paperDistributionDao : new PaperDistributionDao();
        this.calculator = calculator != null ? calculator : new PaperRequirementCalculator();
        this.auditPublisher = auditPublisher;
    }

    /**
     * Creates a new question paper set with field validation and duplicate prevention.
     */
    public QuestionPaper createPaperSet(Long examId, Long subjectId, String setCode, int totalCopies, Long actorUserId)
            throws SQLException, ValidationException {
        validatePaperInput(examId, subjectId, setCode, totalCopies);

        String normalizedSet = setCode.trim().toUpperCase();
        if (questionPaperDao.existsDuplicateSet(examId, subjectId, normalizedSet, null)) {
            throw new ValidationException("Paper set '" + normalizedSet + "' already exists for this examination and subject.");
        }

        QuestionPaper paper = new QuestionPaper(examId, subjectId, normalizedSet, totalCopies);
        Long id = questionPaperDao.insert(paper);
        paper.setId(id);

        if (auditPublisher != null && actorUserId != null) {
            String details = String.format("{\"examId\":%d,\"subjectId\":%d,\"setCode\":\"%s\",\"totalCopies\":%d}",
                    examId, subjectId, normalizedSet, totalCopies);
            auditPublisher.publish("CREATE_PAPER_SET", String.valueOf(actorUserId), "QUESTION_PAPER", id, details);
        }

        return paper;
    }

    /**
     * Updates printed copies / stock for a question paper set.
     */
    public void updateStock(Long paperId, int newTotalCopies, Long actorUserId) throws SQLException, ValidationException {
        if (paperId == null) {
            throw new ValidationException("Question paper ID is required.");
        }
        if (newTotalCopies < 0) {
            throw new ValidationException("Total copies cannot be negative.");
        }

        Optional<QuestionPaper> existing = questionPaperDao.findById(paperId);
        if (existing.isEmpty()) {
            throw new ValidationException("Question paper not found with ID: " + paperId);
        }

        int prevCopies = existing.get().getTotalCopies();
        questionPaperDao.updateStock(paperId, newTotalCopies);

        if (auditPublisher != null && actorUserId != null) {
            String details = String.format("{\"paperId\":%d,\"previousCopies\":%d,\"newCopies\":%d}",
                    paperId, prevCopies, newTotalCopies);
            auditPublisher.publish("UPDATE_PAPER_STOCK", String.valueOf(actorUserId), "QUESTION_PAPER", paperId, details);
        }
    }

    public List<QuestionPaper> getPapersForExam(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        return questionPaperDao.findByExamId(examId);
    }

    public List<QuestionPaper> getAllPapers() throws SQLException {
        return questionPaperDao.findAll();
    }

    public Optional<QuestionPaper> getPaperById(Long id) throws SQLException {
        if (id == null) {
            return Optional.empty();
        }
        return questionPaperDao.findById(id);
    }

    // =========================================================================
    // Implementation of PaperQueryService contract
    // =========================================================================

    @Override
    public String getPaperRequirements(Long examId) {
        if (examId == null) {
            return "[]";
        }
        try {
            List<RoomPaperRequirementDTO> requirements = calculator.calculateRequirements(examId, null);
            return GSON.toJson(requirements);
        } catch (SQLException ex) {
            LOGGER.error("Failed to query paper requirements for exam {}", examId, ex);
            return "[]";
        }
    }

    @Override
    public int getRequiredCopies(Long roomId, Long subjectId) {
        if (roomId == null || subjectId == null) {
            return 0;
        }
        try {
            return paperDistributionDao.getRequiredCopies(roomId, subjectId);
        } catch (SQLException ex) {
            LOGGER.error("Failed to query required copies for room {} and subject {}", roomId, subjectId, ex);
            return 0;
        }
    }

    @Override
    public boolean isDistributionComplete(Long roomId) {
        if (roomId == null) {
            return false;
        }
        try {
            return paperDistributionDao.isDistributionCompleteForRoom(roomId);
        } catch (SQLException ex) {
            LOGGER.error("Failed to check distribution completion for room {}", roomId, ex);
            return false;
        }
    }

    /**
     * Validates that total printed stock for every question paper set in an exam
     * is sufficient to cover aggregate requirements across all allocated examination rooms.
     */
    public List<String> validateAggregateStockSufficiency(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        List<QuestionPaper> papers = questionPaperDao.findByExamId(examId);
        List<String> shortfalls = new java.util.ArrayList<>();
        for (QuestionPaper qp : papers) {
            int aggregateRequired = paperDistributionDao.getAggregateRequiredForPaper(examId, qp.getId());
            if (qp.getTotalCopies() < aggregateRequired) {
                int deficit = aggregateRequired - qp.getTotalCopies();
                String msg = String.format("Stock deficit for Set '%s' (Subject %d): Required across rooms=%d, available=%d (Deficit: %d)",
                        qp.getSetCode(), qp.getSubjectId(), aggregateRequired, qp.getTotalCopies(), deficit);
                shortfalls.add(msg);
            }
        }
        return shortfalls;
    }

    private void validatePaperInput(Long examId, Long subjectId, String setCode, int totalCopies)
            throws ValidationException {
        if (examId == null || examId <= 0) {
            throw new ValidationException("A valid Examination ID is required.");
        }
        if (subjectId == null || subjectId <= 0) {
            throw new ValidationException("A valid Subject ID is required.");
        }
        if (setCode == null || setCode.trim().isEmpty()) {
            throw new ValidationException("Set Code cannot be empty (e.g. Set A, Set B).");
        }
        if (setCode.trim().length() > 50) {
            throw new ValidationException("Set Code cannot exceed 50 characters.");
        }
        if (totalCopies < 0) {
            throw new ValidationException("Available printed stock cannot be negative.");
        }
    }
}
