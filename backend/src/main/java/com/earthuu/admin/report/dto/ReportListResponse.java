package com.earthuu.admin.report.dto;

import com.earthuu.admin.report.entity.Report;
import com.earthuu.admin.report.entity.ReportResolution;
import com.earthuu.admin.report.entity.ReportStatus;
import com.earthuu.admin.report.entity.ReportTargetType;

import java.time.Instant;
import java.util.UUID;

public record ReportListResponse(
        UUID id,
        ReportTargetType targetType,
        UUID targetId,
        String targetName,
        String reasonCode,
        ReportStatus status,
        ReportResolution resolution,
        Instant createdAt,
        Instant resolvedAt,
        int revision
) {
    public static ReportListResponse from(Report report, String targetName) {
        return new ReportListResponse(report.getId(), report.getTargetType(), report.getTargetId(), targetName,
                report.getReasonCode(), report.getStatus(), report.getResolution(), report.getCreatedAt(),
                report.getResolvedAt(), report.getRevision());
    }
}
