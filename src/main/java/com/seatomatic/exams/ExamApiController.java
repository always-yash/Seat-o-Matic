package com.seatomatic.exams;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
public class ExamApiController {
    private final ExamService exams;
    public ExamApiController(ExamService exams) { this.exams = exams; }
    @GetMapping("/rooms") public List<Room> rooms() { return exams.findRooms(); }
    @GetMapping("/exams") public List<Exam> exams() { return exams.findExams(); }
    @GetMapping("/exams/{id}/candidates") public List<ExamCandidate> candidates(@PathVariable Long id) { return exams.findCandidates(id); }
    @PostMapping("/rooms") @ResponseStatus(HttpStatus.CREATED)
    public Room createRoom(@Valid @RequestBody RoomInput input) {
        return exams.createRoom(new ExamService.CreateRoom(input.name(), input.building(), input.rows(), input.columns(), input.capacity()));
    }
    @PostMapping("/exams") @ResponseStatus(HttpStatus.CREATED)
    public Exam createExam(@Valid @RequestBody ExamInput input) {
        return exams.createExam(new ExamService.CreateExam(input.code(), input.name(), input.subject(), input.startsAt(), input.expectedStudents(), input.roomId()));
    }
    @PostMapping("/exams/{id}/candidates") @ResponseStatus(HttpStatus.CREATED)
    public ExamCandidate registerCandidate(@PathVariable Long id, @Valid @RequestBody CandidateInput input) {
        return exams.registerCandidate(id, input.studentId());
    }
    public record RoomInput(@NotBlank @Size(max = 80) String name, @NotBlank @Size(max = 100) String building,
                            @Min(1) @Max(50) int rows, @Min(1) @Max(50) int columns, @Min(1) @Max(2500) int capacity) { }
    public record ExamInput(@NotBlank @Size(max = 40) String code, @NotBlank @Size(max = 140) String name,
                            @NotBlank @Size(max = 120) String subject, @NotNull LocalDateTime startsAt,
                            @Min(1) @Max(2500) int expectedStudents, @NotNull Long roomId) { }
    public record CandidateInput(@NotBlank @Size(max = 32) String studentId) { }
}
