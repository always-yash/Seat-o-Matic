package com.seatomatic.papers;

import com.seatomatic.exams.Exam;
import com.seatomatic.exams.Room;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jakarta.persistence.Column;

@Entity
@Table(name = "paper_runs")
public class PaperRun {
    public enum Status { PREPARING, READY, DISTRIBUTED, COLLECTED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne private Exam exam;
    @ManyToOne private Room room;
    private String setCode;
    private int requiredCopies;
    private int bufferCopies;
    @Enumerated(EnumType.STRING) private Status status = Status.PREPARING;
    @Column(name = "updated_at") private LocalDateTime updatedAt = LocalDateTime.now();
    protected PaperRun() { }
    public PaperRun(Exam exam, Room room, String setCode, int requiredCopies, int bufferCopies) {
        this.exam = exam; this.room = room; this.setCode = setCode; this.requiredCopies = requiredCopies;
        this.bufferCopies = bufferCopies; this.status = Status.PREPARING; this.updatedAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public Exam getExam() { return exam; }
    public Room getRoom() { return room; }
    public String getSetCode() { return setCode; }
    public int getRequiredCopies() { return requiredCopies; }
    public int getBufferCopies() { return bufferCopies; }
    public Status getStatus() { return status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void transitionTo(Status next) {
        if (next.ordinal() != status.ordinal() + 1) throw new IllegalArgumentException("Paper runs can only move forward one step in the workflow.");
        status = next;
        updatedAt = LocalDateTime.now();
    }
    public boolean updateRequirement(int required, int buffer) {
        if (requiredCopies == required && bufferCopies == buffer) return false;
        if (status == Status.DISTRIBUTED || status == Status.COLLECTED) {
            throw new IllegalArgumentException("A paper run already in distribution cannot be recalculated. Resolve its handover first.");
        }
        requiredCopies = required; bufferCopies = buffer; status = Status.PREPARING; updatedAt = LocalDateTime.now();
        return true;
    }
}
