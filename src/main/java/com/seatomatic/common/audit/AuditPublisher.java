package com.seatomatic.common.audit;

public interface AuditPublisher {

    void publish(String action, String actor, String targetType, Long targetId, String details);
}
