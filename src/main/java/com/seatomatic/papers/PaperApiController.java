package com.seatomatic.papers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/papers")
@PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
public class PaperApiController {
    private final PaperRunService paperRuns;
    public PaperApiController(PaperRunService paperRuns) { this.paperRuns = paperRuns; }
    @PostMapping("/{id}/transition") public PaperRun transition(@PathVariable Long id, @Valid @RequestBody PaperTransition input) {
        return paperRuns.transition(id, input.status());
    }
    public record PaperTransition(@NotNull PaperRun.Status status) { }
}
