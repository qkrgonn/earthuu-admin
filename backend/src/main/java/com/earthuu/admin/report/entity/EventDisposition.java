package com.earthuu.admin.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_dispositions")
public class EventDisposition {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "report_id", nullable = false) private UUID reportId;
    @Column(name = "event_id") private UUID eventId;
    @Enumerated(EnumType.STRING) @Column(name = "target_type", nullable = false, length = 20) private ReportTargetType targetType;
    @Column(name = "target_id", nullable = false) private UUID targetId;
    @Column(name = "actor_id", nullable = false) private UUID actorId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private ReportResolution action;
    @Column(nullable = false, columnDefinition = "text") private String reason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected EventDisposition() {}

    private EventDisposition(UUID reportId, UUID eventId, ReportTargetType targetType, UUID targetId,
                             UUID actorId, ReportResolution action, String reason) {
        this.reportId = reportId;
        this.eventId = eventId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.actorId = actorId;
        this.action = action;
        this.reason = reason;
    }

    public static EventDisposition record(Report report, UUID eventId, UUID actorId, ReportResolution action, String reason) {
        return new EventDisposition(report.getId(), eventId, report.getTargetType(), report.getTargetId(), actorId, action, reason);
    }

    @PrePersist void prePersist() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getReportId() { return reportId; }
    public UUID getActorId() { return actorId; }
    public ReportResolution getAction() { return action; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
