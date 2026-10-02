package com.earthuu.admin.event.dto;

import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.entity.ModerationStatus;

import java.time.Instant;
import java.util.UUID;

public record EventReviewResponse(
        UUID eventId,
        ModerationStatus moderationStatus,
        int revision,
        Instant updatedAt
) {
    public static EventReviewResponse from(Event event) {
        return new EventReviewResponse(event.getId(), event.getModerationStatus(), event.getRevision(), event.getUpdatedAt());
    }
}
