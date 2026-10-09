package com.seatomatic.exam.contract;

public interface ExamQueryService {

    String findExamSummary(Long examId);

    boolean isExamConfigured(Long examId);

    Long getRoomCount(Long examId);
}
