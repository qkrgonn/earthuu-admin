package com.earthuu.admin.event.entity;

import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "host_id", nullable = false)
    private UUID hostId;

    @Column(name = "current_version_id")
    private UUID currentVersionId;

    @Column(name = "published_version_id")
    private UUID publishedVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserProfile hostProfile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id", insertable = false, updatable = false)
    private EventVersion currentVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false, length = 30)
    private ModerationStatus moderationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", nullable = false, length = 30)
    private LifecycleStatus lifecycleStatus;

    @Version
    @Column(nullable = false)
    private int revision;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Event() {
    }

    private Event(UUID hostId) {
        this.hostId = hostId;
        this.moderationStatus = ModerationStatus.PENDING;
        this.lifecycleStatus = LifecycleStatus.NOT_OPEN;
    }

    public static Event createPending(UUID hostId) {
        return new Event(hostId);
    }

    public void attachCurrentVersion(UUID versionId) {
        this.currentVersionId = versionId;
    }

    public void startReview() {
        if (moderationStatus == ModerationStatus.REVIEWING) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_STARTED);
        }
        requireStatus(ModerationStatus.PENDING);
        moderationStatus = ModerationStatus.REVIEWING;
    }

    public void approve() {
        requireStatus(ModerationStatus.REVIEWING);
        moderationStatus = ModerationStatus.APPROVED;
        publishedVersionId = currentVersionId;
        publishedAt = Instant.now();
    }

    public void reject() {
        requireStatus(ModerationStatus.REVIEWING);
        moderationStatus = ModerationStatus.REJECTED;
    }

    public void suspend() {
        if (lifecycleStatus == LifecycleStatus.CANCELLED || lifecycleStatus == LifecycleStatus.ENDED) {
            throw new BusinessException(ErrorCode.INVALID_EVENT_OPERATION);
        }
        lifecycleStatus = LifecycleStatus.SUSPENDED;
    }

    public void discard() {
        if (lifecycleStatus == LifecycleStatus.CANCELLED || lifecycleStatus == LifecycleStatus.ENDED) {
            throw new BusinessException(ErrorCode.INVALID_EVENT_OPERATION);
        }
        lifecycleStatus = LifecycleStatus.CANCELLED;
    }

    private void requireStatus(ModerationStatus expected) {
        if (moderationStatus != expected) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_STATUS);
        }
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
    public UUID getHostId() { return hostId; }
    public UUID getCurrentVersionId() { return currentVersionId; }
    public UUID getPublishedVersionId() { return publishedVersionId; }
    public UserProfile getHostProfile() { return hostProfile; }
    public EventVersion getCurrentVersion() { return currentVersion; }
    public ModerationStatus getModerationStatus() { return moderationStatus; }
    public LifecycleStatus getLifecycleStatus() { return lifecycleStatus; }
    public int getRevision() { return revision; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
