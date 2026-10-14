package com.seatomatic.papers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/papers")
@PreAuthorize("hasAnyRole('ADMIN','FACULTY','INVIGILATOR')")
public class PaperApiController {
    private final PaperRunService paperRuns;
    private final InvigilatorAssignmentService assignmentService;

    public PaperApiController(PaperRunService paperRuns, InvigilatorAssignmentService assignmentService) {
        this.paperRuns = paperRuns;
        this.assignmentService = assignmentService;
    }

    @PostMapping("/{id}/transition")
    public PaperRun transition(@PathVariable Long id,
                               @Valid @RequestBody PaperTransition input,
                               Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INVIGILATOR"))) {
            PaperRun run = paperRuns.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Paper run not found."));
            if (!assignmentService.isInvigilatorAssignedToExamAndRoom(
                    authentication.getName(), run.getExam().getId(), run.getRoom().getId())) {
                throw new AccessDeniedException("Invigilators can only modify workflow actions for their assigned examination rooms.");
            }
        }
        return paperRuns.transition(id, input.status());
    }

    public record PaperTransition(@NotNull PaperRun.Status status) { }
}
