package com.seatomatic.exams;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamCandidateRepository extends JpaRepository<ExamCandidate, Long> {
    List<ExamCandidate> findByExamIdOrderByStudentFullName(Long examId);
}
