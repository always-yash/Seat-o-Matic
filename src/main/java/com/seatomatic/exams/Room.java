package com.seatomatic.exams;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "rooms")
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank private String name;
    @NotBlank private String building;
    @Min(1) @Column(name = "seat_rows") private int rows;
    @Min(1) @Column(name = "seat_columns") private int columns;
    @Min(1) private int capacity;
    private boolean active = true;

    protected Room() { }
    public Room(String name, String building, int rows, int columns, int capacity) {
        this.name = name; this.building = building; this.rows = rows; this.columns = columns; this.capacity = capacity;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBuilding() { return building; }
    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public int getCapacity() { return capacity; }
    public boolean isActive() { return active; }
}
