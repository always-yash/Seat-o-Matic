package com.seatomatic.exams;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "exams")
public class Exam {
    public enum Status { DRAFT, READY, LOCKED, COMPLETED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank private String code;
    @NotBlank private String name;
    @NotBlank private String subject;
    private LocalDateTime startsAt;
    @Min(0) private int expectedStudents;
    @Column(name = "registration_closed", nullable = false) private boolean registrationClosed;
    @Enumerated(EnumType.STRING) private Status status = Status.DRAFT;
    @ManyToOne private Room room;

    protected Exam() { }
    public Exam(String code, String name, String subject, LocalDateTime startsAt, int expectedStudents, Room room) {
        this.code = code; this.name = name; this.subject = subject; this.startsAt = startsAt;
        this.expectedStudents = expectedStudents; this.room = room;
    }
    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getSubject() { return subject; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public int getExpectedStudents() { return expectedStudents; }
    public Status getStatus() { return status; }
    public Room getRoom() { return room; }
    public boolean isRegistrationClosed() { return registrationClosed; }
    public void closeCandidateRegistration() { registrationClosed = true; }
}
