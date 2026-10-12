package com.seatomatic.paper.model;

import java.sql.Timestamp;

/**
 * Domain model representing a question paper set associated with an exam and subject.
 */
public class QuestionPaper {
    private Long id;
    private Long examId;
    private Long subjectId;
    private String setCode;
    private int totalCopies;
    private Timestamp createdAt;

    // Transient presentation fields
    private String examTitle;
    private String subjectCode;
    private String subjectName;

    public QuestionPaper() {
    }

    public QuestionPaper(Long examId, Long subjectId, String setCode, int totalCopies) {
        this.examId = examId;
        this.subjectId = subjectId;
        this.setCode = setCode;
        this.totalCopies = totalCopies;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSetCode() {
        return setCode;
    }

    public void setSetCode(String setCode) {
        this.setCode = setCode;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    @Override
    public String toString() {
        return "QuestionPaper{" +
                "id=" + id +
                ", examId=" + examId +
                ", subjectId=" + subjectId +
                ", setCode='" + setCode + '\'' +
                ", totalCopies=" + totalCopies +
                '}';
    }
}
