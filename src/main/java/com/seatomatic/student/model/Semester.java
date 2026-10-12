package com.seatomatic.student.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Academic semester entity representation.
 */
public class Semester implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer number;
    private String name;

    public Semester() {
    }

    public Semester(Long id, Integer number, String name) {
        this.id = id;
        this.number = number;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Semester semester)) return false;
        return Objects.equals(id, semester.id) && Objects.equals(number, semester.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number);
    }

    @Override
    public String toString() {
        return "Semester{" +
                "id=" + id +
                ", number=" + number +
                ", name='" + name + '\'' +
                '}';
    }
}

