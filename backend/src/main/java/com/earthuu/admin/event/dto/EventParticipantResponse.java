package com.earthuu.admin.event.dto;

import com.earthuu.admin.event.entity.EventParticipation;

import java.time.Instant;
import java.util.UUID;

public record EventParticipantResponse(
        UUID participationId,
        UUID userId,
        String name,
        String quotaGroup,
        String nationality,
        String status,
        String attendance,
        Instant confirmedAt
) {
    public static EventParticipantResponse from(EventParticipation participation) {
        return new EventParticipantResponse(participation.getId(), participation.getUserId(),
                participation.getProfile() == null ? null : participation.getProfile().getName(),
                participation.getQuotaGroup(), participation.getNationalitySnapshot(), participation.getStatus(),
                participation.getAttendance(), participation.getConfirmedAt());
    }
}
