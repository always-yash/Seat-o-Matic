package com.seatomatic.student.contract;

public interface StudentQueryService {

    String findEligibleStudents(Long examId);

    int countByGroup(String branchCode);

    Long getById(Long studentId);
}
