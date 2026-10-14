package com.seatomatic.core;

import com.seatomatic.analysis.AuditService;
import com.seatomatic.exams.ExamService;
import com.seatomatic.papers.PaperRunService;
import com.seatomatic.student.StudentService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.seatomatic.core.UserAccountService;
import java.util.HashMap;
import java.util.Map;
import com.seatomatic.papers.InvigilatorAssignmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WorkspaceController {
    private final StudentService students;
    private final ExamService exams;
    private final PaperRunService papers;
    private final AuditService audit;
    private final SeatingPlanRepository plans;
    private final UserAccountService userAccounts;
    private final InvigilatorAssignmentService assignmentService;

    public WorkspaceController(StudentService students, ExamService exams, PaperRunService papers,
            AuditService audit, SeatingPlanRepository plans, UserAccountService userAccounts,
            InvigilatorAssignmentService assignmentService) {
        this.students = students;
        this.exams = exams;
        this.papers = papers;
        this.audit = audit;
        this.plans = plans;
        this.userAccounts = userAccounts;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/workspace/{section}")
    public String workspace(@PathVariable String section, Model model, Authentication authentication) {
        if (!java.util.Set.of("students", "exams", "rooms", "papers", "analytics", "team").contains(section))
            return "redirect:/";
        boolean admin = hasRole(authentication, "ROLE_ADMIN");
        boolean faculty = hasRole(authentication, "ROLE_FACULTY");
        if ((java.util.Set.of("students", "exams", "rooms", "papers").contains(section) && !admin && !faculty)
                || (java.util.Set.of("analytics", "team").contains(section) && !admin))
            throw new AccessDeniedException("This workspace is not available to your account.");
        model.addAttribute("page", section);
        switch (section) {
            case "students" -> model.addAttribute("records", students.findAll());
            case "exams" -> {
                var records = exams.findExams();
                model.addAttribute("records", records);
                model.addAttribute("rooms", exams.findRooms());
                Map<Long, Long> planIds = new HashMap<>();
                records.forEach(exam -> plans.findByExamId(exam.getId())
                        .ifPresent(plan -> planIds.put(exam.getId(), plan.getId())));
                model.addAttribute("planIds", planIds);
                Map<Long, String> invigilatorMap = new HashMap<>();
                assignmentService.listCoverage().forEach(c -> {
                    if (c.assigned()) invigilatorMap.put(c.examId(), c.invigilatorUsername());
                });
                model.addAttribute("invigilatorMap", invigilatorMap);
            }
            case "rooms" -> model.addAttribute("records", exams.findRooms());
            case "papers" -> model.addAttribute("records", papers.findAll());
            case "team" -> {
                var accounts = userAccounts.list();
                model.addAttribute("records", accounts);
                model.addAttribute("accounts", accounts);
                model.addAttribute("assignments", assignmentService.listCoverage());
                model.addAttribute("invigilators", accounts.stream()
                        .filter(a -> a.active() && a.role() == UserAccount.Role.INVIGILATOR).toList());
            }
            case "analytics" -> {
                model.addAttribute("studentCount", students.count());
                model.addAttribute("examCount", exams.countExams());
                model.addAttribute("roomCount", exams.countRooms());
                model.addAttribute("paperCount", papers.count());
                model.addAttribute("activity", audit.latest());
            }
        }
        return "workspace";
    }

    @GetMapping("/workspace/team")
    @PreAuthorize("hasRole('ADMIN')")
    public String team(Model model) {
        model.addAttribute("page", "team");
        var accounts = userAccounts.list();
        model.addAttribute("accounts", accounts);
        model.addAttribute("assignments", assignmentService.listCoverage());
        model.addAttribute("invigilators", accounts.stream()
                .filter(a -> a.active() && a.role() == UserAccount.Role.INVIGILATOR).toList());
        return "team";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    @GetMapping("/workspace/plans/{id}/edit")
    public String redirectPlanEdit(@PathVariable Long id) {
        return "redirect:/workspace/plans/" + id;
    }
}
