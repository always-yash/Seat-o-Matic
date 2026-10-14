package com.seatomatic.papers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invigilator-assignments")
public class InvigilatorAssignmentApiController {
    private final InvigilatorAssignmentService assignmentService;

    public InvigilatorAssignmentApiController(InvigilatorAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<InvigilatorAssignmentService.ExamRoomAssignmentView> listAll() {
        return assignmentService.listCoverage();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public InvigilatorAssignmentService.AssignmentView assign(@Valid @RequestBody AssignInput input) {
        return assignmentService.assign(input.examId(), input.roomId(), input.userId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void remove(@PathVariable Long id) {
        assignmentService.remove(id);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('INVIGILATOR')")
    public List<InvigilatorAssignmentService.InvigilatorScheduleView> myAssignments(Authentication authentication) {
        return assignmentService.findForInvigilator(authentication.getName());
    }

    public record AssignInput(@NotNull Long examId, @NotNull Long roomId, @NotNull Long userId) { }
}
