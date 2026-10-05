package com.earthuu.admin.report.entity;

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

import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_restrictions")
public class UserRestriction {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "source_report_id", nullable = false, unique = true) private UUID sourceReportId;
    @Column(name = "imposed_by", nullable = false) private UUID imposedBy;
    @Column(nullable = false, columnDefinition = "text") private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserRestrictionStatus status;
    @Column(name = "starts_at", nullable = false) private Instant startsAt;
    @Column(name = "ends_at") private Instant endsAt;
    @Column(name = "revoked_at") private Instant revokedAt;
    @Column(name = "revoked_by") private UUID revokedBy;
    @Column(name = "revoke_reason", columnDefinition = "text") private String revokeReason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private int revision;

    protected UserRestriction() {}

    private UserRestriction(UUID userId, UUID sourceReportId, UUID imposedBy, String reason, Instant endsAt) {
        this.userId = userId;
        this.sourceReportId = sourceReportId;
        this.imposedBy = imposedBy;
        this.reason = reason;
        this.status = UserRestrictionStatus.ACTIVE;
        this.startsAt = Instant.now();
        this.endsAt = endsAt;
    }

    public static UserRestriction impose(UUID userId, UUID sourceReportId, UUID imposedBy,
                                         String reason, Instant endsAt) {
        return new UserRestriction(userId, sourceReportId, imposedBy, reason, endsAt);
    }

    public void revoke(UUID actorId, String revokeReason, Instant now) {
        if (!isActiveAt(now)) throw new BusinessException(ErrorCode.RESTRICTION_NOT_ACTIVE);
        status = UserRestrictionStatus.REVOKED;
        revokedAt = now;
        revokedBy = actorId;
        this.revokeReason = revokeReason;
    }

    public boolean isActiveAt(Instant now) {
        return status == UserRestrictionStatus.ACTIVE && (endsAt == null || endsAt.isAfter(now));
    }

    public UserRestrictionStatus effectiveStatusAt(Instant now) {
        return status == UserRestrictionStatus.ACTIVE && endsAt != null && !endsAt.isAfter(now)
                ? UserRestrictionStatus.EXPIRED : status;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getSourceReportId() { return sourceReportId; }
    public UUID getImposedBy() { return imposedBy; }
    public String getReason() { return reason; }
    public UserRestrictionStatus getStatus() { return status; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public UUID getRevokedBy() { return revokedBy; }
    public String getRevokeReason() { return revokeReason; }
    public int getRevision() { return revision; }
}
