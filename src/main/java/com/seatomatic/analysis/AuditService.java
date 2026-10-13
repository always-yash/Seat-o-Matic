package com.seatomatic.analysis;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AuditService {
    private final AuditEventRepository events;
    public AuditService(AuditEventRepository events) { this.events = events; }
    @Transactional(readOnly = true) public List<AuditEvent> latest() { return events.findTop5ByOrderByCreatedAtDesc(); }
    @Transactional(readOnly = true) public long count() { return events.count(); }
    @Transactional public void record(String action, String entityType, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null ? "system" : authentication.getName();
        events.save(new AuditEvent(actor, action, entityType, details));
    }
}
