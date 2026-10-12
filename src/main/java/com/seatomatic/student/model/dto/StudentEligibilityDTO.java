package com.seatomatic.student.model.dto;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

/**
 * Representation of a student's eligibility and seating allocation for a specific examination.
 */
public class StudentEligibilityDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long studentId;
    private String studentName;
    private String enrollmentNo;
    private Long examId;
    private String examTitle;
    private Date examDate;
    private Time startTime;
    private Time endTime;
    private String examStatus;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private boolean eligible;
    private boolean seated;
    private String roomCode;
    private Integer seatRow;
    private Integer seatCol;

    public StudentEligibilityDTO() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(String enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public String getExamStatus() {
        return examStatus;
    }

    public void setExamStatus(String examStatus) {
        this.examStatus = examStatus;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
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

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public boolean isSeated() {
        return seated;
    }

    public void setSeated(boolean seated) {
        this.seated = seated;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public Integer getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(Integer seatRow) {
        this.seatRow = seatRow;
    }

    public Integer getSeatCol() {
        return seatCol;
    }

    public void setSeatCol(Integer seatCol) {
        this.seatCol = seatCol;
    }

    public String getSeatLocation() {
        if (!seated || roomCode == null) {
            return "Unassigned";
        }
        if (seatRow != null && seatCol != null) {
            return String.format("%s (Row %d, Col %d)", roomCode, seatRow, seatCol);
        }
        return roomCode;
    }
}
