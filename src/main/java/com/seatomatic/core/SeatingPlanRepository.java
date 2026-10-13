package com.seatomatic.core;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatingPlanRepository extends JpaRepository<SeatingPlan, Long> {
    Optional<SeatingPlan> findByExamId(Long examId);
}
