package com.seatomatic.core;

import com.seatomatic.student.Student;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "seat_assignments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"plan_id", "seat_row", "seat_column"}),
        @UniqueConstraint(columnNames = {"plan_id", "student_student_id"})
})
public class SeatAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private SeatingPlan plan;
    @ManyToOne(optional = false) private Student student;
    private int seatRow;
    private int seatColumn;
    protected SeatAssignment() { }
    public SeatAssignment(Student student, int seatRow, int seatColumn) {
        this.student = student; this.seatRow = seatRow; this.seatColumn = seatColumn;
    }
    void setPlan(SeatingPlan plan) { this.plan = plan; }
    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public int getSeatRow() { return seatRow; }
    public int getSeatColumn() { return seatColumn; }
    public void moveTo(int row, int column) { seatRow = row; seatColumn = column; }
}
