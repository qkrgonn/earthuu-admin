package com.earthuu.admin.event.dto;

import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.entity.LifecycleStatus;
import com.earthuu.admin.event.entity.ModerationStatus;
import com.earthuu.admin.event.entity.EventVisibility;

import java.time.Instant;
import java.util.UUID;

public record EventListResponse(
        UUID id,
        String title,
        UUID hostId,
        String hostName,
        String universityName,
        UUID categoryId,
        String venueName,
        Instant startsAt,
        Instant submittedAt,
        ModerationStatus moderationStatus,
        LifecycleStatus lifecycleStatus,
        EventVisibility visibilityStatus,
        int revision
) {
    public static EventListResponse from(Event event) {
        var version = event.getCurrentVersion();
        var profile = event.getHostProfile();
        return new EventListResponse(
                event.getId(), version.getTitle(), event.getHostId(),
                profile == null ? null : profile.getName(),
                profile == null ? null : profile.getUniversityName(),
                version.getCategoryId(), version.getVenueName(), version.getStartsAt(), version.getSubmittedAt(),
                event.getModerationStatus(), event.getLifecycleStatus(), event.getVisibilityStatus(), event.getRevision());
    }
}
