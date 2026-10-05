package com.earthuu.admin.user.dto;

import com.earthuu.admin.user.entity.UserStatusAction;
import java.time.Instant;
import java.util.UUID;

public record UserStatusActionResponse(UUID id, UUID actorId, String action, String previousStatus,
                                       String newStatus, String reason, Instant createdAt) {
    public static UserStatusActionResponse from(UserStatusAction action) {
        return new UserStatusActionResponse(action.getId(), action.getActorId(), action.getAction().name(),
                action.getPreviousStatus().name(), action.getNewStatus().name(), action.getReason(), action.getCreatedAt());
    }
}
