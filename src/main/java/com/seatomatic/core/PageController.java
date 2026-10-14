package com.seatomatic.core;

import com.seatomatic.analysis.AuditService;
import com.seatomatic.core.SeatingService.PlanView;
import com.seatomatic.exams.Exam;
import com.seatomatic.exams.ExamService;
import com.seatomatic.papers.PaperRunService;
import com.seatomatic.student.StudentService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@Controller
public class PageController {
    private final StudentService students;
    private final ExamService exams;
    private final PaperRunService paperRuns;
    private final AuditService audit;
    private final SeatingService seating;
    private final SeatingPlanRepository plans;

    public PageController(StudentService students, ExamService exams, PaperRunService paperRuns,
                          AuditService audit, SeatingService seating, SeatingPlanRepository plans) {
        this.students = students; this.exams = exams; this.paperRuns = paperRuns;
        this.audit = audit; this.seating = seating; this.plans = plans;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_INVIGILATOR"))) {
            model.addAttribute("page", "overview");
            return "invigilator-access";
        }
        model.addAttribute("studentCount", students.count());
        model.addAttribute("examCount", exams.countExams());
        model.addAttribute("roomCount", exams.countRooms());
        model.addAttribute("paperCount", paperRuns.count());
        var upcoming = exams.findUpcoming();
        model.addAttribute("upcomingExams", upcoming);
        if (!upcoming.isEmpty()) {
            long days = Duration.between(LocalDateTime.now(), upcoming.get(0).getStartsAt()).toDays();
            model.addAttribute("nextExamLabel", days < 0 ? "PAST SESSION · REVIEW SCHEDULE" : days == 0 ? "TODAY · NEXT SESSION" : "NEXT UP · IN " + days + (days == 1 ? " DAY" : " DAYS"));
        } else model.addAttribute("nextExamLabel", "NO UPCOMING EXAMS");
        model.addAttribute("rooms", exams.findFeaturedRooms());
        model.addAttribute("activity", audit.latest());
        model.addAttribute("page", "overview");
        return "dashboard";
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/workspace/plans/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public String seatingPlan(@PathVariable Long id, Model model) {
        PlanView plan = seating.find(id);
        model.addAttribute("plan", plan);
        Map<String, PlanView.SeatView> occupied = new HashMap<>();
        plan.assignments().forEach(seat -> occupied.put(seat.row() + ":" + seat.column(), seat));
        List<SeatCell> cells = new ArrayList<>();
        for (int row = 1; row <= plan.rows(); row++) {
            for (int column = 1; column <= plan.columns(); column++) {
                int seatIndex = (row - 1) * plan.columns() + ((row % 2 == 1) ? column : plan.columns() - column + 1);
                cells.add(new SeatCell(row, column, seatIndex <= plan.capacity(), occupied.get(row + ":" + column)));
            }
        }
        model.addAttribute("cells", cells);
        model.addAttribute("page", "exams");
        return "seating-plan";
    }

    @GetMapping("/workspace/exams/{id}/candidates")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public String candidates(@PathVariable Long id, Model model) {
        Exam exam = exams.findExam(id);
        var roster = exams.findCandidates(id);
        var seatingPlan = plans.findByExamId(id);
        model.addAttribute("exam", exam);
        model.addAttribute("candidates", roster);
        model.addAttribute("registeredIds", roster.stream().map(candidate -> candidate.getStudent().getStudentId()).toList());
        model.addAttribute("students", students.findAll().stream().filter(student -> student.isActive()).toList());
        model.addAttribute("hasPlan", seatingPlan.isPresent());
        model.addAttribute("planId", seatingPlan.map(SeatingPlan::getId).orElse(null));
        model.addAttribute("page", "exams");
        return "candidates";
    }

    @GetMapping({"/workspace/rooms/{id}", "/workspace/rooms/{id}/layout"})
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public String roomLayout(@PathVariable Long id, Model model) {
        var room = exams.findRoom(id);
        model.addAttribute("room", room);
        List<SeatCell> cells = new ArrayList<>();
        for (int row = 1; row <= room.getRows(); row++) {
            for (int column = 1; column <= room.getColumns(); column++) {
                int seatIndex = (row - 1) * room.getColumns() + ((row % 2 == 1) ? column : room.getColumns() - column + 1);
                cells.add(new SeatCell(row, column, seatIndex <= room.getCapacity(), null));
            }
        }
        model.addAttribute("cells", cells);
        model.addAttribute("page", "rooms");
        return "seating-plan";
    }

    public record SeatCell(int row, int column, boolean usable, PlanView.SeatView assignment) { }
}
