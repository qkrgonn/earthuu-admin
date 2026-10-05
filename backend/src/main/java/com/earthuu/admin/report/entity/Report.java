package com.earthuu.admin.report.entity;

import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "reporter_id", nullable = false) private UUID reporterId;
    @Enumerated(EnumType.STRING) @Column(name = "target_type", nullable = false, length = 20) private ReportTargetType targetType;
    @Column(name = "target_id", nullable = false) private UUID targetId;
    @Column(name = "reason_code", nullable = false, length = 50) private String reasonCode;
    @Column(nullable = false, columnDefinition = "text") private String description;
    @Column(columnDefinition = "text") private String evidence;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ReportStatus status;
    @Column(name = "reviewer_id") private UUID reviewerId;
    @Column(name = "review_started_at") private Instant reviewStartedAt;
    @Enumerated(EnumType.STRING) @Column(length = 40) private ReportResolution resolution;
    @Column(name = "resolution_note", columnDefinition = "text") private String resolutionNote;
    @Column(name = "resolved_by") private UUID resolvedBy;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Version @Column(nullable = false) private int revision;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Report() {}

    private Report(UUID reporterId, ReportTargetType targetType, UUID targetId, String reasonCode,
                   String description, String evidence) {
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reasonCode = reasonCode;
        this.description = description;
        this.evidence = evidence;
        this.status = ReportStatus.RECEIVED;
    }

    public static Report create(UUID reporterId, ReportTargetType targetType, UUID targetId,
                                String reasonCode, String description, String evidence) {
        return new Report(reporterId, targetType, targetId, reasonCode, description, evidence);
    }

    public void startReview(UUID actorId) {
        if (status != ReportStatus.RECEIVED) throw new BusinessException(ErrorCode.INVALID_REPORT_STATUS);
        status = ReportStatus.INVESTIGATING;
        reviewerId = actorId;
        reviewStartedAt = Instant.now();
    }

    public void resolve(ReportResolution decision, String note, UUID actorId) {
        if (status != ReportStatus.INVESTIGATING) throw new BusinessException(ErrorCode.INVALID_REPORT_STATUS);
        if (!decision.supports(targetType)) throw new BusinessException(ErrorCode.INVALID_REPORT_RESOLUTION);
        status = ReportStatus.RESOLVED;
        resolution = decision;
        resolutionNote = note;
        resolvedBy = actorId;
        resolvedAt = Instant.now();
    }

    @PrePersist void prePersist() { var now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getReporterId() { return reporterId; }
    public ReportTargetType getTargetType() { return targetType; }
    public UUID getTargetId() { return targetId; }
    public String getReasonCode() { return reasonCode; }
    public String getDescription() { return description; }
    public String getEvidence() { return evidence; }
    public ReportStatus getStatus() { return status; }
    public UUID getReviewerId() { return reviewerId; }
    public Instant getReviewStartedAt() { return reviewStartedAt; }
    public ReportResolution getResolution() { return resolution; }
    public String getResolutionNote() { return resolutionNote; }
    public UUID getResolvedBy() { return resolvedBy; }
    public Instant getResolvedAt() { return resolvedAt; }
    public int getRevision() { return revision; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
