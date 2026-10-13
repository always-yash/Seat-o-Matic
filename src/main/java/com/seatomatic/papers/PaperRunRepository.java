package com.seatomatic.papers;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaperRunRepository extends JpaRepository<PaperRun, Long> {
    Optional<PaperRun> findByExamIdAndRoomIdAndSetCode(Long examId, Long roomId, String setCode);
}
