package com.seatomatic.analysis;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String actor;
    private String action;
    private String entityType;
    private String details;
    private LocalDateTime createdAt = LocalDateTime.now();
    protected AuditEvent() { }
    public AuditEvent(String actor, String action, String entityType, String details) {
        this.actor = actor; this.action = action; this.entityType = entityType; this.details = details;
    }
    public Long getId() { return id; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
