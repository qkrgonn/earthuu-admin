package com.earthuu.admin.report.dto;

import com.earthuu.admin.report.entity.Report;
import com.earthuu.admin.report.entity.ReportResolution;
import com.earthuu.admin.report.entity.ReportStatus;

import java.time.Instant;
import java.util.UUID;

public record ReportActionResponse(
        UUID id,
        ReportStatus status,
        ReportResolution resolution,
        UUID reviewerId,
        UUID resolvedBy,
        Instant reviewStartedAt,
        Instant resolvedAt,
        int revision
) {
    public static ReportActionResponse from(Report report) {
        return new ReportActionResponse(report.getId(), report.getStatus(), report.getResolution(),
                report.getReviewerId(), report.getResolvedBy(), report.getReviewStartedAt(),
                report.getResolvedAt(), report.getRevision());
    }
}
