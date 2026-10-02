package com.earthuu.admin.event.dto;

import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.entity.ModerationAction;
import com.earthuu.admin.event.entity.ModerationActionType;

import java.time.Instant;
import java.util.UUID;

public record ReviewedEventResponse(
        UUID id,
        String title,
        String hostName,
        String universityName,
        ModerationActionType decision,
        String reason,
        Instant reviewedAt
) {
    public static ReviewedEventResponse from(Event event, ModerationAction action) {
        var profile = event.getHostProfile();
        return new ReviewedEventResponse(
                event.getId(),
                event.getCurrentVersion().getTitle(),
                profile == null ? null : profile.getName(),
                profile == null ? null : profile.getUniversityName(),
                action.getAction(),
                action.getReason(),
                action.getCreatedAt()
        );
    }
}
