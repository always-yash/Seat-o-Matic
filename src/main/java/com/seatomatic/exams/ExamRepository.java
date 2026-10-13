package com.seatomatic.exams;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findTop5ByOrderByStartsAtAsc();
}
