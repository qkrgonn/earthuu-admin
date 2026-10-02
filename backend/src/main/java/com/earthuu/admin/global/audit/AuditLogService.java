package com.earthuu.admin.global.audit;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void recordEventAction(UUID actorId, String action, UUID eventId, Map<String, Object> metadata) {
        repository.save(AuditLog.record(actorId, action, "EVENT", eventId, metadata));
    }
}
