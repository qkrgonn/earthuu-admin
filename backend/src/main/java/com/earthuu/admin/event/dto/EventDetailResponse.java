package com.earthuu.admin.event.dto;

import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.entity.LifecycleStatus;
import com.earthuu.admin.event.entity.ModerationStatus;
import com.earthuu.admin.event.entity.SubmissionState;
import com.earthuu.admin.event.entity.EventVisibility;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventDetailResponse(
        UUID id,
        UUID hostId,
        String hostName,
        String universityName,
        ModerationStatus moderationStatus,
        LifecycleStatus lifecycleStatus,
        EventVisibility visibilityStatus,
        Instant hiddenAt,
        UUID hiddenBy,
        int revision,
        UUID currentVersionId,
        int versionNumber,
        SubmissionState submissionState,
        String title,
        UUID categoryId,
        String cityCode,
        String venueName,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant startsAt,
        Instant endsAt,
        String timezone,
        Instant recruitmentStart,
        Instant recruitmentEnd,
        String descriptionKo,
        String descriptionEn,
        Instant submittedAt,
        Instant publishedAt,
        List<ModerationHistoryResponse> moderationHistory
) {
    public static EventDetailResponse from(Event event, List<ModerationHistoryResponse> history) {
        var version = event.getCurrentVersion();
        var profile = event.getHostProfile();
        return new EventDetailResponse(
                event.getId(), event.getHostId(), profile == null ? null : profile.getName(),
                profile == null ? null : profile.getUniversityName(), event.getModerationStatus(),
                event.getLifecycleStatus(), event.getVisibilityStatus(), event.getHiddenAt(), event.getHiddenBy(),
                event.getRevision(), version.getId(), version.getVersionNumber(),
                version.getSubmissionState(), version.getTitle(), version.getCategoryId(), version.getCityCode(),
                version.getVenueName(), version.getAddress(), version.getLatitude(), version.getLongitude(),
                version.getStartsAt(), version.getEndsAt(), version.getTimezone(), version.getRecruitmentStart(),
                version.getRecruitmentEnd(), version.getDescriptionKo(), version.getDescriptionEn(),
                version.getSubmittedAt(), event.getPublishedAt(), history);
    }
}
