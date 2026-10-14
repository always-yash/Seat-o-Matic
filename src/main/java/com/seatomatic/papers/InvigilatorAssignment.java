package com.seatomatic.papers;

import com.seatomatic.core.UserAccount;
import com.seatomatic.exams.Exam;
import com.seatomatic.exams.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "invigilator_assignments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_invig_exam_room", columnNames = {"exam_id", "room_id"}),
        @UniqueConstraint(name = "uq_invig_exam_user", columnNames = {"exam_id", "user_id"})
})
public class InvigilatorAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount invigilator;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    protected InvigilatorAssignment() { }

    public InvigilatorAssignment(Exam exam, Room room, UserAccount invigilator) {
        this.exam = exam;
        this.room = room;
        this.invigilator = invigilator;
        this.assignedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Exam getExam() { return exam; }
    public Room getRoom() { return room; }
    public UserAccount getInvigilator() { return invigilator; }
    public LocalDateTime getAssignedAt() { return assignedAt; }

    public void setInvigilator(UserAccount invigilator) {
        this.invigilator = invigilator;
    }
}
