package com.earthuu.admin.dashboard.dto;

import com.earthuu.admin.global.audit.AuditLog;

import java.time.Instant;
import java.util.UUID;

public record AdminActivityResponse(UUID id, UUID actorId, String action, String targetType,
                                    UUID targetId, Instant createdAt) {
    public static AdminActivityResponse from(AuditLog log) {
        return new AdminActivityResponse(log.getId(), log.getActorId(), log.getAction(),
                log.getTargetType(), log.getTargetId(), log.getCreatedAt());
    }
}
