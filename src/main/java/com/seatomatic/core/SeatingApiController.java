package com.seatomatic.core;

import com.seatomatic.core.SeatingPlan.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
public class SeatingApiController {
    private final SeatingService seating;
    public SeatingApiController(SeatingService seating) { this.seating = seating; }
    @GetMapping("/exams/{id}/seating-plan") public SeatingService.PlanView byExam(@PathVariable Long id) { return seating.findForExam(id); }
    @GetMapping("/seating-plans/{id}") public SeatingService.PlanView byId(@PathVariable Long id) { return seating.find(id); }
    @PostMapping("/exams/{id}/seating-plan/generate") @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public SeatingService.PlanView generate(@PathVariable Long id) { return seating.generate(id); }
    @PostMapping("/seating-plans/{id}/move") @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public SeatingService.PlanView move(@PathVariable Long id, @Valid @RequestBody SeatMove move) {
        return seating.move(id, move.assignmentId(), move.row(), move.column());
    }
    @PostMapping("/seating-plans/{id}/transition") @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public SeatingService.PlanView transition(@PathVariable Long id, @Valid @RequestBody PlanTransition transition) {
        return seating.transition(id, transition.status());
    }
    public record SeatMove(@NotNull Long assignmentId, @Min(1) int row, @Min(1) int column) { }
    public record PlanTransition(@NotNull Status status) { }
}
