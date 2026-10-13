package com.seatomatic.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "students")
public class Student {
    @Id @NotBlank private String studentId;
    @NotBlank private String fullName;
    @NotBlank private String branch;
    private String section;
    private int semester;
    private boolean active = true;

    protected Student() { }
    public Student(String studentId, String fullName, String branch, String section, int semester) {
        this.studentId = studentId; this.fullName = fullName; this.branch = branch;
        this.section = section; this.semester = semester; this.active = true;
    }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getBranch() { return branch; }
    public String getSection() { return section; }
    public int getSemester() { return semester; }
    public boolean isActive() { return active; }
}
