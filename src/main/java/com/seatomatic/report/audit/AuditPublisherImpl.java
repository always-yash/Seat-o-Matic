package com.seatomatic.report.audit;

import com.seatomatic.common.audit.AuditPublisher;

public class AuditPublisherImpl implements AuditPublisher {

    @Override
    public void publish(String action, String actor, String targetType, Long targetId, String details) {
        // Stub implementation for M5 to complete later.
    }
}
