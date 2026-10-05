package com.earthuu.admin.report.dto;

import com.earthuu.admin.report.entity.EventDisposition;
import com.earthuu.admin.report.entity.ReportResolution;

import java.time.Instant;
import java.util.UUID;

public record DispositionHistoryResponse(
        UUID id,
        UUID actorId,
        ReportResolution action,
        String reason,
        Instant createdAt
) {
    public static DispositionHistoryResponse from(EventDisposition disposition) {
        return new DispositionHistoryResponse(disposition.getId(), disposition.getActorId(),
                disposition.getAction(), disposition.getReason(), disposition.getCreatedAt());
    }
}
