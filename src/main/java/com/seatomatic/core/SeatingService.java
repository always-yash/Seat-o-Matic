package com.seatomatic.core;

import com.seatomatic.analysis.AuditService;
import com.seatomatic.papers.PaperRunService;
import com.seatomatic.exams.Exam;
import com.seatomatic.exams.ExamService;
import com.seatomatic.exams.Room;
import com.seatomatic.student.Student;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

@Service
public class SeatingService {
    private final ExamService exams;
    private final SeatingPlanRepository plans;
    private final AuditService audit;
    private final EntityManager entityManager;
    private final PaperRunService paperRuns;

    public SeatingService(ExamService exams, SeatingPlanRepository plans,
                          AuditService audit, EntityManager entityManager, PaperRunService paperRuns) {
        this.exams = exams; this.plans = plans; this.audit = audit; this.entityManager = entityManager; this.paperRuns = paperRuns;
    }

    @Transactional
    public PlanView generate(Long examId) {
        Exam exam = exams.findExam(examId);
        Room room = exam.getRoom();
        if (room == null) throw new IllegalArgumentException("Assign a room before generating a seating plan.");
        List<Student> roster = exams.findCandidates(examId).stream().map(candidate -> candidate.getStudent()).toList();
        if (roster.isEmpty()) throw new IllegalArgumentException("Register eligible students before generating a seating plan.");
        if (roster.size() != exam.getExpectedStudents()) {
            throw new IllegalArgumentException("The roster has " + roster.size() + " students, but the examination expects " + exam.getExpectedStudents() + ". Update one before generating a plan.");
        }
        int gridSize = room.getRows() * room.getColumns();
        if (roster.size() > room.getCapacity() || roster.size() > gridSize) {
            throw new IllegalArgumentException("Room capacity is " + room.getCapacity() + " for " + roster.size() + " eligible students. Assign another room or adjust the roster.");
        }

        SeatingPlan plan = plans.findByExamId(examId).orElseGet(() -> new SeatingPlan(exam));
        if (plan.getStatus() == SeatingPlan.Status.LOCKED) throw new IllegalArgumentException("This plan is locked. A locked plan cannot be regenerated.");
        List<Student> ordered = interleaveBranches(roster);
        List<SeatAssignment> arrangement = new ArrayList<>();
        Map<String, SeatAssignment> occupancy = new HashMap<>();
        List<Seat> seats = usableSeats(room);
        for (Student student : ordered) {
            Seat best = null;
            int bestPenalty = Integer.MAX_VALUE;
            for (Seat seat : seats) {
                String key = seat.row() + ":" + seat.column();
                if (occupancy.containsKey(key)) continue;
                int penalty = localPenalty(student, seat, occupancy);
                if (penalty < bestPenalty) { best = seat; bestPenalty = penalty; }
                if (bestPenalty == 0) break;
            }
            if (best == null) throw new IllegalStateException("No usable seat remained during allocation.");
            SeatAssignment assignment = new SeatAssignment(student, best.row(), best.column());
            arrangement.add(assignment);
            occupancy.put(best.row() + ":" + best.column(), assignment);
        }
        int score = score(arrangement);
        paperRuns.prepareRequirement(exam, room, roster.size());
        exams.closeCandidateRegistration(examId);
        plan.clearAssignments();
        entityManager.flush();
        plan.replaceAssignments(arrangement, score);
        SeatingPlan saved = plans.save(plan);
        audit.record("SEATING_PLAN_GENERATED", "Seating plan", exam.getCode() + " · " + roster.size() + " students · score " + score);
        return view(saved);
    }

    @Transactional(readOnly = true)
    public PlanView findForExam(Long examId) {
        return view(plans.findByExamId(examId).orElseThrow(() -> new IllegalArgumentException("No seating plan exists for this examination.")));
    }

    @Transactional(readOnly = true)
    public PlanView find(Long planId) {
        return view(plans.findById(planId).orElseThrow(() -> new IllegalArgumentException("Seating plan not found.")));
    }

    @Transactional(readOnly = true)
    public List<LockedPlanReportRow> lockedPlanReportRows() {
        return plans.findAllByStatusOrderByUpdatedAtDesc(SeatingPlan.Status.LOCKED).stream()
                .flatMap(plan -> plan.getAssignments().stream().map(assignment -> {
                    Exam exam = plan.getExam();
                    Student student = assignment.getStudent();
                    return new LockedPlanReportRow(exam.getCode(), exam.getName(), exam.getStartsAt(),
                            exam.getRoom().getName(), assignment.getSeatRow(), assignment.getSeatColumn(),
                            student.getStudentId(), student.getFullName(), student.getBranch(),
                            student.getSection(), student.getSemester());
                }))
                .toList();
    }

    @Transactional
    public PlanView transition(Long planId, SeatingPlan.Status next) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (next == SeatingPlan.Status.LOCKED && (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))) {
            throw new AccessDeniedException("Only an Admin can lock the final seating plan.");
        }
        SeatingPlan plan = plans.findById(planId).orElseThrow(() -> new IllegalArgumentException("Seating plan not found."));
        plan.transitionTo(next);
        audit.record("PLAN_" + next, "Seating plan", plan.getExam().getCode() + " · plan " + planId);
        return view(plan);
    }

    @Transactional
    public PlanView move(Long planId, Long assignmentId, int targetRow, int targetColumn) {
        SeatingPlan plan = plans.findById(planId).orElseThrow(() -> new IllegalArgumentException("Seating plan not found."));
        if (plan.getStatus() == SeatingPlan.Status.LOCKED) throw new IllegalArgumentException("This plan is locked and cannot be changed.");
        Room room = plan.getExam().getRoom();
        if (targetRow < 1 || targetRow > room.getRows() || targetColumn < 1 || targetColumn > room.getColumns()) {
            throw new IllegalArgumentException("Choose a seat inside the room grid.");
        }
        if (usableSeats(room).stream().noneMatch(seat -> seat.row() == targetRow && seat.column() == targetColumn)) {
            throw new IllegalArgumentException("That seat is outside this room's usable capacity.");
        }
        SeatAssignment moving = plan.getAssignments().stream().filter(a -> a.getId().equals(assignmentId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seat assignment not found in this plan."));
        if (plan.getAssignments().stream().anyMatch(a -> a.getSeatRow() == targetRow && a.getSeatColumn() == targetColumn)) {
            throw new IllegalArgumentException("Choose an empty seat for this move.");
        }
        moving.moveTo(targetRow, targetColumn);
        int score = score(plan.getAssignments());
        plan.updateAssignments(score);
        audit.record("SEAT_MOVED", "Seating plan", moving.getStudent().getStudentId() + " moved to row " + targetRow + ", seat " + targetColumn);
        return view(plan);
    }

    private List<Student> interleaveBranches(List<Student> roster) {
        Map<String, List<Student>> groups = new TreeMap<>();
        roster.stream().sorted(Comparator.comparing(Student::getStudentId))
                .forEach(student -> groups.computeIfAbsent(student.getBranch(), ignored -> new ArrayList<>()).add(student));
        List<Student> ordered = new ArrayList<>(roster.size());
        List<String> branches = new ArrayList<>(groups.keySet());
        int index = 0;
        while (ordered.size() < roster.size()) {
            for (String branch : branches) {
                List<Student> group = groups.get(branch);
                if (index < group.size()) ordered.add(group.get(index));
            }
            index++;
        }
        return ordered;
    }

    private int localPenalty(Student student, Seat seat, Map<String, SeatAssignment> occupancy) {
        int penalty = 0;
        for (int[] delta : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            SeatAssignment neighbour = occupancy.get((seat.row() + delta[0]) + ":" + (seat.column() + delta[1]));
            if (neighbour == null) continue;
            if (student.getBranch().equals(neighbour.getStudent().getBranch())) penalty += 10;
            if (sameSection(student, neighbour.getStudent())) penalty += 2;
        }
        return penalty;
    }

    private int score(List<SeatAssignment> assignments) {
        int score = 0;
        for (int i = 0; i < assignments.size(); i++) {
            SeatAssignment left = assignments.get(i);
            for (int j = i + 1; j < assignments.size(); j++) {
                SeatAssignment right = assignments.get(j);
                int distance = Math.abs(left.getSeatRow() - right.getSeatRow()) + Math.abs(left.getSeatColumn() - right.getSeatColumn());
                if (distance != 1) continue;
                if (left.getStudent().getBranch().equals(right.getStudent().getBranch())) score += 10;
                if (sameSection(left.getStudent(), right.getStudent())) score += 2;
            }
        }
        return score;
    }

    private List<Seat> usableSeats(Room room) {
        List<Seat> seats = new ArrayList<>(room.getCapacity());
        for (int row = 1; row <= room.getRows() && seats.size() < room.getCapacity(); row++) {
            if (row % 2 == 1) for (int col = 1; col <= room.getColumns() && seats.size() < room.getCapacity(); col++) seats.add(new Seat(row, col));
            else for (int col = room.getColumns(); col >= 1 && seats.size() < room.getCapacity(); col--) seats.add(new Seat(row, col));
        }
        return seats;
    }

    private boolean sameSection(Student left, Student right) {
        return left.getBranch().equals(right.getBranch()) && left.getSection() != null && left.getSection().equals(right.getSection());
    }

    private PlanView view(SeatingPlan plan) {
        List<PlanView.SeatView> seats = plan.getAssignments().stream()
                .sorted(Comparator.comparingInt(SeatAssignment::getSeatRow).thenComparingInt(SeatAssignment::getSeatColumn))
                .map(a -> new PlanView.SeatView(a.getId(), a.getStudent().getStudentId(), a.getStudent().getFullName(), a.getStudent().getBranch(), a.getSeatRow(), a.getSeatColumn()))
                .toList();
        return new PlanView(plan.getId(), plan.getExam().getId(), plan.getExam().getCode(), plan.getExam().getName(),
                plan.getExam().getRoom().getName(), plan.getExam().getRoom().getRows(), plan.getExam().getRoom().getColumns(),
                plan.getExam().getRoom().getCapacity(), plan.getStatus(), plan.getConflictScore(), plan.getVersion(),
                plan.getUpdatedAt(), seats);
    }

    private record Seat(int row, int column) { }

    public record LockedPlanReportRow(String examCode, String examName, LocalDateTime startsAt, String roomName,
                                      int seatRow, int seatColumn, String studentId, String studentName,
                                      String branch, String section, int semester) { }

    public record PlanView(Long id, Long examId, String examCode, String examName, String roomName, int rows,
                           int columns, int capacity, SeatingPlan.Status status, int conflictScore, long version,
                           LocalDateTime updatedAt, List<SeatView> assignments) {
        public record SeatView(Long assignmentId, String studentId, String studentName, String branch, int row, int column) { }
    }
}
