package com.seatomatic.student;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/students")
@PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
public class StudentApiController {
    private final StudentService students;
    public StudentApiController(StudentService students) { this.students = students; }
    @GetMapping public List<Student> all() { return students.findAll(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Student create(@Valid @RequestBody StudentInput input) {
        return students.create(new StudentService.CreateStudent(input.studentId(), input.fullName(), input.branch(), input.section(), input.semester()));
    }
    @PostMapping(path = "/import", consumes = MediaType.TEXT_PLAIN_VALUE)
    public StudentService.ImportResult importCsv(@RequestBody String csv) { return students.importCsv(csv); }
    public record StudentInput(@NotBlank @Size(max = 32) String studentId, @NotBlank @Size(max = 120) String fullName,
                               @NotBlank @Size(max = 100) String branch, @Size(max = 30) String section,
                               @Min(1) @Max(12) int semester) { }
}
