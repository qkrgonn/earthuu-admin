package com.earthuu.admin.report.dto;

import com.earthuu.admin.report.entity.Report;
import com.earthuu.admin.report.entity.ReportResolution;
import com.earthuu.admin.report.entity.ReportStatus;
import com.earthuu.admin.report.entity.ReportTargetType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReportDetailResponse(
        UUID id,
        UUID reporterId,
        ReportTargetType targetType,
        UUID targetId,
        String targetName,
        String reasonCode,
        String description,
        String evidence,
        ReportStatus status,
        UUID reviewerId,
        Instant reviewStartedAt,
        ReportResolution resolution,
        String resolutionNote,
        UUID resolvedBy,
        Instant resolvedAt,
        Instant createdAt,
        int revision,
        List<DispositionHistoryResponse> history
) {
    public static ReportDetailResponse from(Report report, String targetName, List<DispositionHistoryResponse> history) {
        return new ReportDetailResponse(report.getId(), report.getReporterId(), report.getTargetType(),
                report.getTargetId(), targetName, report.getReasonCode(), report.getDescription(), report.getEvidence(),
                report.getStatus(), report.getReviewerId(), report.getReviewStartedAt(), report.getResolution(),
                report.getResolutionNote(), report.getResolvedBy(), report.getResolvedAt(), report.getCreatedAt(),
                report.getRevision(), history);
    }
}
