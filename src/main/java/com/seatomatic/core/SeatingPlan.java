package com.seatomatic.core;

import com.seatomatic.exams.Exam;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seating_plans")
public class SeatingPlan {
    public enum Status { DRAFT, REVIEWED, APPROVED, LOCKED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) private Exam exam;
    @Enumerated(EnumType.STRING) private Status status = Status.DRAFT;
    private int conflictScore;
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Version private long version;
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SeatAssignment> assignments = new ArrayList<>();
    protected SeatingPlan() { }
    public SeatingPlan(Exam exam) { this.exam = exam; }
    public Long getId() { return id; }
    public Exam getExam() { return exam; }
    public Status getStatus() { return status; }
    public int getConflictScore() { return conflictScore; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
    public List<SeatAssignment> getAssignments() { return assignments; }
    public void clearAssignments() {
        if (status == Status.LOCKED) throw new IllegalArgumentException("This plan is locked and cannot be regenerated.");
        assignments.clear();
    }
    public void replaceAssignments(List<SeatAssignment> next, int score) {
        if (status == Status.LOCKED) throw new IllegalArgumentException("This plan is locked and cannot be regenerated.");
        assignments.clear();
        next.forEach(assignment -> { assignment.setPlan(this); assignments.add(assignment); });
        conflictScore = score; status = Status.DRAFT; updatedAt = LocalDateTime.now();
    }
    public void updateAssignments(int score) {
        if (status == Status.LOCKED) throw new IllegalArgumentException("This plan is locked and cannot be changed.");
        conflictScore = score; status = Status.DRAFT; updatedAt = LocalDateTime.now();
    }
    public void transitionTo(Status next) {
        if (next.ordinal() != status.ordinal() + 1) throw new IllegalArgumentException("Plans must be reviewed, approved, then locked in order.");
        status = next; updatedAt = LocalDateTime.now();
    }
}
