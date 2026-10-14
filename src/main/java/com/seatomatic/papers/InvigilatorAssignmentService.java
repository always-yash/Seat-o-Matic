package com.seatomatic.papers;

import com.seatomatic.analysis.AuditService;
import com.seatomatic.core.SeatingPlan;
import com.seatomatic.core.SeatingPlanRepository;
import com.seatomatic.core.UserAccount;
import com.seatomatic.core.UserAccountRepository;
import com.seatomatic.exams.Exam;
import com.seatomatic.exams.ExamRepository;
import com.seatomatic.exams.Room;
import com.seatomatic.exams.RoomRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvigilatorAssignmentService {
    private final InvigilatorAssignmentRepository assignments;
    private final ExamRepository exams;
    private final RoomRepository rooms;
    private final UserAccountRepository users;
    private final SeatingPlanRepository seatingPlans;
    private final PaperRunRepository paperRuns;
    private final AuditService audit;

    public InvigilatorAssignmentService(InvigilatorAssignmentRepository assignments,
                                        ExamRepository exams,
                                        RoomRepository rooms,
                                        UserAccountRepository users,
                                        SeatingPlanRepository seatingPlans,
                                        PaperRunRepository paperRuns,
                                        AuditService audit) {
        this.assignments = assignments;
        this.exams = exams;
        this.rooms = rooms;
        this.users = users;
        this.seatingPlans = seatingPlans;
        this.paperRuns = paperRuns;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<ExamRoomAssignmentView> listCoverage() {
        List<Exam> allExams = exams.findAllByOrderByStartsAtAsc();
        List<ExamRoomAssignmentView> views = new ArrayList<>();
        for (Exam exam : allExams) {
            Room room = exam.getRoom();
            if (room == null) continue;
            Optional<InvigilatorAssignment> assignment = assignments.findByExamIdAndRoomId(exam.getId(), room.getId());
            views.add(new ExamRoomAssignmentView(
                    exam.getId(),
                    exam.getCode(),
                    exam.getName(),
                    exam.getSubject(),
                    exam.getStartsAt(),
                    room.getId(),
                    room.getName(),
                    room.getBuilding(),
                    room.getCapacity(),
                    assignment.map(InvigilatorAssignment::getId).orElse(null),
                    assignment.map(a -> a.getInvigilator().getId()).orElse(null),
                    assignment.map(a -> a.getInvigilator().getUsername()).orElse(null),
                    assignment.isPresent()
            ));
        }
        return views;
    }

    @Transactional
    public AssignmentView assign(Long examId, Long roomId, Long userId) {
        if (examId == null || roomId == null || userId == null) {
            throw new IllegalArgumentException("Exam, room, and invigilator user IDs are required.");
        }
        Exam exam = exams.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Examination not found."));
        Room room = rooms.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        if (exam.getRoom() == null || !exam.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("The specified room is not assigned to this examination.");
        }

        UserAccount user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invigilator account not found."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("The selected invigilator account is paused or inactive.");
        }

        if (user.getRole() != UserAccount.Role.INVIGILATOR) {
            throw new IllegalArgumentException("Only accounts with the INVIGILATOR role can be assigned to examination rooms.");
        }

        // Prevent duplicate assignment of the same invigilator to different rooms in the same exam
        Optional<InvigilatorAssignment> existingForUser = assignments.findByExamIdAndInvigilatorId(examId, userId);
        if (existingForUser.isPresent() && !existingForUser.get().getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("This invigilator is already assigned to room " +
                    existingForUser.get().getRoom().getName() + " for this examination.");
        }

        Optional<InvigilatorAssignment> existingForRoom = assignments.findByExamIdAndRoomId(examId, roomId);
        if (existingForRoom.isPresent()) {
            InvigilatorAssignment assignment = existingForRoom.get();
            if (assignment.getInvigilator().getId().equals(userId)) {
                return toView(assignment);
            }
            String previousInvigilator = assignment.getInvigilator().getUsername();
            assignment.setInvigilator(user);
            InvigilatorAssignment saved = assignments.save(assignment);
            audit.record("INVIGILATOR_REASSIGNED", "Invigilator assignment",
                    exam.getCode() + " · " + room.getName() + " · reallocated from " + previousInvigilator + " to " + user.getUsername());
            return toView(saved);
        }

        InvigilatorAssignment saved = assignments.save(new InvigilatorAssignment(exam, room, user));
        audit.record("INVIGILATOR_ASSIGNED", "Invigilator assignment",
                exam.getCode() + " · " + room.getName() + " · assigned to " + user.getUsername());
        return toView(saved);
    }

    @Transactional
    public void remove(Long assignmentId) {
        InvigilatorAssignment assignment = assignments.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invigilator assignment not found."));
        assignments.delete(assignment);
        audit.record("INVIGILATOR_UNASSIGNED", "Invigilator assignment",
                assignment.getExam().getCode() + " · " + assignment.getRoom().getName() + " · unassigned from " + assignment.getInvigilator().getUsername());
    }

    @Transactional(readOnly = true)
    public List<InvigilatorScheduleView> findForInvigilator(String username) {
        return assignments.findByInvigilatorUsername(username).stream().map(a -> {
            Exam exam = a.getExam();
            Room room = a.getRoom();
            var plan = seatingPlans.findByExamId(exam.getId());
            var run = paperRuns.findByExamIdAndRoomIdAndSetCode(exam.getId(), room.getId(), "SET-A");
            return new InvigilatorScheduleView(
                    a.getId(),
                    exam.getId(),
                    exam.getCode(),
                    exam.getName(),
                    exam.getSubject(),
                    exam.getStartsAt(),
                    room.getId(),
                    room.getName(),
                    room.getBuilding(),
                    room.getCapacity(),
                    plan.map(SeatingPlan::getId).orElse(null),
                    plan.map(p -> p.getStatus().name()).orElse("NO_PLAN"),
                    run.map(PaperRun::getId).orElse(null),
                    run.map(r -> r.getStatus().name()).orElse(null)
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public boolean isInvigilatorAssignedToExam(String username, Long examId) {
        return assignments.isInvigilatorAssignedToExam(username, examId);
    }

    @Transactional(readOnly = true)
    public boolean isInvigilatorAssignedToExamAndRoom(String username, Long examId, Long roomId) {
        if (username == null || examId == null || roomId == null) return false;
        return assignments.findByExamIdAndRoomId(examId, roomId)
                .map(a -> a.getInvigilator().getUsername().equalsIgnoreCase(username))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<InvigilatorAssignment> findByExamAndRoom(Long examId, Long roomId) {
        return assignments.findByExamIdAndRoomId(examId, roomId);
    }

    private static AssignmentView toView(InvigilatorAssignment a) {
        return new AssignmentView(
                a.getId(),
                a.getExam().getId(),
                a.getExam().getCode(),
                a.getExam().getName(),
                a.getRoom().getId(),
                a.getRoom().getName(),
                a.getInvigilator().getId(),
                a.getInvigilator().getUsername(),
                a.getAssignedAt()
        );
    }

    public record AssignmentView(
            Long id,
            Long examId,
            String examCode,
            String examName,
            Long roomId,
            String roomName,
            Long invigilatorId,
            String invigilatorUsername,
            LocalDateTime assignedAt
    ) { }

    public record ExamRoomAssignmentView(
            Long examId,
            String examCode,
            String examName,
            String examSubject,
            LocalDateTime examStartsAt,
            Long roomId,
            String roomName,
            String roomBuilding,
            int roomCapacity,
            Long assignmentId,
            Long invigilatorId,
            String invigilatorUsername,
            boolean assigned
    ) { }

    public record InvigilatorScheduleView(
            Long assignmentId,
            Long examId,
            String examCode,
            String examName,
            String examSubject,
            LocalDateTime examStartsAt,
            Long roomId,
            String roomName,
            String roomBuilding,
            int roomCapacity,
            Long planId,
            String planStatus,
            Long paperRunId,
            String paperRunStatus
    ) {
        public InvigilatorScheduleView(
                Long assignmentId, Long examId, String examCode, String examName, String examSubject,
                LocalDateTime examStartsAt, Long roomId, String roomName, String roomBuilding,
                int roomCapacity, Long planId, String planStatus) {
            this(assignmentId, examId, examCode, examName, examSubject, examStartsAt, roomId, roomName,
                    roomBuilding, roomCapacity, planId, planStatus, null, null);
        }
    }
}
