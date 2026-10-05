package com.earthuu.admin.user.entity;

import com.earthuu.admin.auth.entity.UserStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_status_actions")
public class UserStatusAction {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "actor_id", nullable = false) private UUID actorId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserStatusActionType action;
    @Enumerated(EnumType.STRING) @Column(name = "previous_status", nullable = false, length = 20) private UserStatus previousStatus;
    @Enumerated(EnumType.STRING) @Column(name = "new_status", nullable = false, length = 20) private UserStatus newStatus;
    @Column(nullable = false, columnDefinition = "text") private String reason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected UserStatusAction() {}

    private UserStatusAction(UUID userId, UUID actorId, UserStatusActionType action,
                             UserStatus previousStatus, UserStatus newStatus, String reason) {
        this.userId = userId;
        this.actorId = actorId;
        this.action = action;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
    }

    public static UserStatusAction record(UUID userId, UUID actorId, UserStatusActionType action,
                                          UserStatus previousStatus, UserStatus newStatus, String reason) {
        return new UserStatusAction(userId, actorId, action, previousStatus, newStatus, reason);
    }

    @PrePersist void prePersist() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getActorId() { return actorId; }
    public UserStatusActionType getAction() { return action; }
    public UserStatus getPreviousStatus() { return previousStatus; }
    public UserStatus getNewStatus() { return newStatus; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
