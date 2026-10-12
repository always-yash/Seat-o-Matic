package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.common.exception.SecurityException;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.common.security.Role;
import com.seatomatic.paper.dao.InvigilatorAssignmentDao;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.DistributionStatus;
import com.seatomatic.paper.model.InvigilatorAssignment;
import com.seatomatic.paper.model.PaperDistribution;
import com.seatomatic.paper.model.dto.InvigilatorRoomViewDTO;
import com.seatomatic.paper.model.dto.RoomPaperRequirementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service orchestrating invigilator room operations and strictly enforcing
 * room-scoped access control to prevent Insecure Direct Object References (IDOR).
 */
public class InvigilationService extends BaseDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvigilationService.class);

    private final InvigilatorAssignmentDao assignmentDao;
    private final PaperDistributionDao distributionDao;
    private final QuestionPaperDao paperDao;
    private final PaperRequirementCalculator calculator;
    private final DistributionWorkflowService workflowService;
    private final AuditPublisher auditPublisher;

    public InvigilationService() {
        this(new InvigilatorAssignmentDao(), new PaperDistributionDao(), new QuestionPaperDao(),
                new PaperRequirementCalculator(), new DistributionWorkflowService(), null);
    }

    public InvigilationService(InvigilatorAssignmentDao assignmentDao,
                              PaperDistributionDao distributionDao,
                              QuestionPaperDao paperDao,
                              PaperRequirementCalculator calculator,
                              DistributionWorkflowService workflowService,
                              AuditPublisher auditPublisher) {
        this.assignmentDao = assignmentDao != null ? assignmentDao : new InvigilatorAssignmentDao();
        this.distributionDao = distributionDao != null ? distributionDao : new PaperDistributionDao();
        this.paperDao = paperDao != null ? paperDao : new QuestionPaperDao();
        this.calculator = calculator != null ? calculator : new PaperRequirementCalculator();
        this.workflowService = workflowService != null ? workflowService : new DistributionWorkflowService();
        this.auditPublisher = auditPublisher;
    }

    /**
     * IDOR Security Guard: Asserts that the authenticated user possesses authorization
     * for the target room within the specified examination.
     */
    public void verifyAccess(Long examId, Long roomId, Long authenticatedUserId, Role authenticatedRole)
            throws SecurityException, SQLException {
        if (authenticatedUserId == null || authenticatedRole == null) {
            throw new SecurityException("Authentication is required to access examination room data.");
        }
        if (examId == null || roomId == null) {
            throw new SecurityException("Examination ID and Room ID are mandatory.");
        }

        // ADMIN has supervisory oversight over all rooms
        if (authenticatedRole == Role.ADMIN) {
            return;
        }

        // FACULTY has supervisory oversight across departmental exam rooms
        if (authenticatedRole == Role.FACULTY) {
            return;
        }

        // INVIGILATOR role: Strictly enforce room assignment ownership
        if (authenticatedRole == Role.INVIGILATOR) {
            boolean assigned = assignmentDao.isAssigned(examId, roomId, authenticatedUserId);
            if (!assigned) {
                LOGGER.warn("IDOR attempt blocked: Invigilator userId={} is not assigned to examId={} roomId={}",
                        authenticatedUserId, examId, roomId);
                if (auditPublisher != null) {
                    auditPublisher.publish("IDOR_ACCESS_DENIED", String.valueOf(authenticatedUserId),
                            "EXAM_ROOM", roomId,
                            String.format("{\"examId\":%d,\"attemptedRoomId\":%d,\"role\":\"INVIGILATOR\"}", examId, roomId));
                }
                throw new SecurityException("Access Denied: You are not authorized to access Room " + roomId +
                        " for this examination.");
            }
            return;
        }

        throw new SecurityException("Unauthorized access attempt with role: " + authenticatedRole);
    }

    /**
     * Retrieves the room-scoped dashboard data for an assigned invigilator.
     * Enforces verifyAccess prior to data retrieval.
     */
    public InvigilatorRoomViewDTO getRoomView(Long examId, Long roomId, Long authenticatedUserId, Role authenticatedRole)
            throws Exception {
        verifyAccess(examId, roomId, authenticatedUserId, authenticatedRole);

        InvigilatorRoomViewDTO dto = new InvigilatorRoomViewDTO();
        dto.setExamId(examId);
        dto.setRoomId(roomId);
        dto.setInvigilatorId(authenticatedUserId);

        // Load exam metadata
        String examSql = "SELECT title, exam_date, start_time, end_time FROM exams WHERE id = ?";
        queryOne(examSql, rs -> {
            dto.setExamTitle(rs.getString("title"));
            dto.setExamDate(String.valueOf(rs.getDate("exam_date")));
            dto.setStartTime(String.valueOf(rs.getTime("start_time")));
            dto.setEndTime(String.valueOf(rs.getTime("end_time")));
            return null;
        }, examId);

        // Load room metadata
        String roomSql = "SELECT room_code, total_rows, total_columns FROM rooms WHERE id = ?";
        queryOne(roomSql, rs -> {
            dto.setRoomCode(rs.getString("room_code"));
            dto.setTotalRows(rs.getInt("total_rows"));
            dto.setTotalColumns(rs.getInt("total_columns"));
            dto.setCapacity(rs.getInt("total_rows") * rs.getInt("total_columns"));
            return null;
        }, roomId);

        // Load active plan ID
        Long planId = calculator.findActivePlanId(examId);

        // Load distribution requirements for this room
        List<RoomPaperRequirementDTO> roomRequirements = new ArrayList<>();
        if (planId != null) {
            List<PaperDistribution> distributions = distributionDao.findByPlanAndRoom(planId, roomId);
            if (!distributions.isEmpty()) {
                for (PaperDistribution d : distributions) {
                    RoomPaperRequirementDTO req = new RoomPaperRequirementDTO();
                    req.setExamId(examId);
                    req.setRoomId(roomId);
                    req.setRoomCode(dto.getRoomCode());
                    req.setPaperId(d.getPaperId());
                    req.setPaperSetCode(d.getSetCode());
                    req.setSubjectId(d.getSubjectId());
                    req.setSubjectCode(d.getSubjectCode());
                    req.setSubjectName(d.getSubjectName());
                    req.setBufferCount(d.getBuffer());
                    req.setTotalRequired(d.getRequired());
                    req.setDistributedCount(d.getDistributed());
                    req.setCollectedCount(d.getCollected());
                    req.setDistributionStatus(d.getStatus());
                    req.setStudentCount(Math.max(0, d.getRequired() - d.getBuffer()));
                    roomRequirements.add(req);
                }
            } else {
                // If not persisted yet, calculate on the fly
                List<RoomPaperRequirementDTO> calculated = calculator.calculateRequirements(examId, null);
                for (RoomPaperRequirementDTO c : calculated) {
                    if (c.getRoomId().equals(roomId)) {
                        roomRequirements.add(c);
                    }
                }
            }

            // Load authorized assigned student enrollment numbers
            String studentsSql =
                    "SELECT DISTINCT s.enrollment_no " +
                    "FROM seat_assignments sa " +
                    "JOIN seats st ON sa.seat_id = st.id " +
                    "JOIN students s ON sa.student_id = s.id " +
                    "WHERE sa.plan_id = ? AND st.room_id = ? AND st.is_blocked = FALSE " +
                    "ORDER BY s.enrollment_no ASC";
            List<String> studentRolls = queryList(studentsSql, rs -> rs.getString("enrollment_no"), planId, roomId);
            dto.setStudentRollNumbers(studentRolls);
        }

        dto.setRequirements(roomRequirements);
        dto.recalculateTotals();

        // Load invigilator assignment metadata
        Optional<InvigilatorAssignment> assignOpt = assignmentDao.findByExamRoomAndInvigilator(examId, roomId, authenticatedUserId);
        if (assignOpt.isPresent()) {
            dto.setAssignmentStatus(assignOpt.get().getStatus());
            dto.setInvigilatorName(assignOpt.get().getInvigilatorName());
        }

        return dto;
    }

    /**
     * Submits answer scripts collection count and verifies reconciliation.
     * Enforces IDOR security check on POST submission.
     */
    public void submitRoomCollection(Long examId, Long roomId, Long distributionId, int collectedCount,
                                    Long authenticatedUserId, Role authenticatedRole) throws Exception {
        // Strict IDOR verification on state-modifying action
        verifyAccess(examId, roomId, authenticatedUserId, authenticatedRole);

        Optional<PaperDistribution> distOpt = distributionDao.findById(distributionId);
        if (distOpt.isEmpty()) {
            throw new ValidationException("Distribution record not found: " + distributionId);
        }
        PaperDistribution dist = distOpt.get();

        // Room boundary integrity: Ensure the distribution record belongs to the requested room
        if (!dist.getRoomId().equals(roomId)) {
            throw new SecurityException("Cross-room tampering blocked: Distribution " + distributionId +
                    " does not belong to room " + roomId);
        }

        // Delegate to guarded state machine
        workflowService.recordCollectionAndVerify(distributionId, collectedCount, authenticatedUserId);
    }

    /**
     * Assigns an invigilator to an exam room (Administrative/Faculty action).
     */
    public InvigilatorAssignment assignInvigilator(Long examId, Long roomId, Long invigilatorId, Long actorUserId)
            throws Exception {
        if (examId == null || roomId == null || invigilatorId == null) {
            throw new ValidationException("Exam, Room, and Invigilator IDs are required.");
        }

        // Check if assignment already exists
        Optional<InvigilatorAssignment> existing = assignmentDao.findByExamRoomAndInvigilator(examId, roomId, invigilatorId);
        if (existing.isPresent()) {
            return existing.get();
        }

        InvigilatorAssignment assignment = new InvigilatorAssignment(examId, roomId, invigilatorId);
        Long id = assignmentDao.insert(assignment);
        assignment.setId(id);

        if (auditPublisher != null && actorUserId != null) {
            String details = String.format("{\"examId\":%d,\"roomId\":%d,\"invigilatorId\":%d}",
                    examId, roomId, invigilatorId);
            auditPublisher.publish("ASSIGN_INVIGILATOR", String.valueOf(actorUserId), "INVIGILATOR_ASSIGNMENT", id, details);
        }

        return assignment;
    }

    public List<InvigilatorAssignment> getAssignmentsForExam(Long examId) throws SQLException {
        if (examId == null) {
            return List.of();
        }
        return assignmentDao.findByExamId(examId);
    }

    public List<InvigilatorAssignment> getAssignmentsForInvigilator(Long invigilatorId) throws SQLException {
        if (invigilatorId == null) {
            return List.of();
        }
        return assignmentDao.findByInvigilatorId(invigilatorId);
    }
}
