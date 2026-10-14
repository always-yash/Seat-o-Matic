package com.seatomatic.papers;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvigilatorAssignmentRepository extends JpaRepository<InvigilatorAssignment, Long> {
    Optional<InvigilatorAssignment> findByExamIdAndRoomId(Long examId, Long roomId);

    Optional<InvigilatorAssignment> findByExamIdAndInvigilatorId(Long examId, Long invigilatorId);

    List<InvigilatorAssignment> findByExamId(Long examId);

    List<InvigilatorAssignment> findByInvigilatorId(Long invigilatorId);

    boolean existsByExamIdAndRoomId(Long examId, Long roomId);

    boolean existsByExamIdAndInvigilatorId(Long examId, Long invigilatorId);

    @Query("SELECT a FROM InvigilatorAssignment a WHERE LOWER(a.invigilator.username) = LOWER(:username) ORDER BY a.exam.startsAt ASC")
    List<InvigilatorAssignment> findByInvigilatorUsername(@Param("username") String username);

    @Query("SELECT COUNT(a) > 0 FROM InvigilatorAssignment a WHERE LOWER(a.invigilator.username) = LOWER(:username) AND a.exam.id = :examId")
    boolean isInvigilatorAssignedToExam(@Param("username") String username, @Param("examId") Long examId);

    @Query("SELECT a FROM InvigilatorAssignment a ORDER BY a.exam.startsAt ASC")
    List<InvigilatorAssignment> findAllOrderByExamStartsAt();
}
