package com.earthuu.admin.event.dto;

import com.earthuu.admin.event.entity.ModerationAction;
import com.earthuu.admin.event.entity.ModerationActionType;

import java.time.Instant;
import java.util.UUID;

public record ModerationHistoryResponse(
        UUID id,
        UUID actorId,
        ModerationActionType action,
        String reason,
        Instant createdAt
) {
    public static ModerationHistoryResponse from(ModerationAction action) {
        return new ModerationHistoryResponse(action.getId(), action.getActorId(), action.getAction(),
                action.getReason(), action.getCreatedAt());
    }
}
