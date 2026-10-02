package com.earthuu.admin.event.entity;

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
@Table(name = "moderation_actions")
public class ModerationAction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "event_id", nullable = false) private UUID eventId;
    @Column(name = "version_id", nullable = false) private UUID versionId;
    @Column(name = "actor_id", nullable = false) private UUID actorId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30) private ModerationActionType action;
    @Column(columnDefinition = "text") private String reason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected ModerationAction() {
    }

    private ModerationAction(UUID eventId, UUID versionId, UUID actorId, ModerationActionType action, String reason) {
        this.eventId = eventId;
        this.versionId = versionId;
        this.actorId = actorId;
        this.action = action;
        this.reason = reason;
    }

    public static ModerationAction record(UUID eventId, UUID versionId, UUID actorId, ModerationActionType action, String reason) {
        return new ModerationAction(eventId, versionId, actorId, action, reason);
    }

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getActorId() { return actorId; }
    public ModerationActionType getAction() { return action; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
