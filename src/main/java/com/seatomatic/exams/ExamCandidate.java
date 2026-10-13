package com.seatomatic.exams;

import com.seatomatic.student.Student;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "exam_candidates", uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "student_student_id"}))
public class ExamCandidate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Exam exam;
    @ManyToOne(optional = false) private Student student;
    protected ExamCandidate() { }
    public ExamCandidate(Exam exam, Student student) { this.exam = exam; this.student = student; }
    public Long getId() { return id; }
    public Exam getExam() { return exam; }
    public Student getStudent() { return student; }
}
