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

    public void recordReportAction(UUID actorId, String action, UUID reportId, Map<String, Object> metadata) {
        repository.save(AuditLog.record(actorId, action, "REPORT", reportId, metadata));
    }

    public void recordUserAction(UUID actorId, String action, UUID userId, Map<String, Object> metadata) {
        repository.save(AuditLog.record(actorId, action, "USER", userId, metadata));
    }

    public void recordVerificationAction(UUID actorId, String action, UUID verificationId,
                                         Map<String, Object> metadata) {
        repository.save(AuditLog.record(actorId, action, "STUDENT_VERIFICATION", verificationId, metadata));
    }

    public void recordMasterDataAction(UUID actorId, String action, String targetType, UUID targetId,
                                       Map<String, Object> metadata) {
        repository.save(AuditLog.record(actorId, action, targetType, targetId, metadata));
    }
}
