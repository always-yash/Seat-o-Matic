package com.seatomatic.core;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatingPlanRepository extends JpaRepository<SeatingPlan, Long> {
    Optional<SeatingPlan> findByExamId(Long examId);
    @EntityGraph(attributePaths = {"exam.room", "assignments.student"})
    List<SeatingPlan> findAllByStatusOrderByUpdatedAtDesc(SeatingPlan.Status status);
}
