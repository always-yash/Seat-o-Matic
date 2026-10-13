package com.seatomatic.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface StudentRepository extends JpaRepository<Student, String> {
    long countByActiveTrue();
    @Query("select lower(student.studentId) from Student student where lower(student.studentId) in :studentIds")
    List<String> findExistingStudentIds(@Param("studentIds") Set<String> studentIds);
}
