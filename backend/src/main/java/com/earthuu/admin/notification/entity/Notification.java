package com.earthuu.admin.notification.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false, length = 50) private String type;
    @Column(name = "target_type", length = 30) private String targetType;
    @Column(name = "target_id") private UUID targetId;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, columnDefinition = "text") private String body;
    @Column(name = "dedupe_key", nullable = false, unique = true, length = 255) private String dedupeKey;
    @Column(name = "read_at") private Instant readAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Notification() {}
    private Notification(UUID userId, String type, String targetType, UUID targetId,
                         String title, String body, String dedupeKey) {
        this.userId = userId; this.type = type; this.targetType = targetType; this.targetId = targetId;
        this.title = title; this.body = body; this.dedupeKey = dedupeKey;
    }
    public static Notification create(UUID userId, String type, String targetType, UUID targetId,
                                      String title, String body, String dedupeKey) {
        return new Notification(userId, type, targetType, targetId, title, body, dedupeKey);
    }
    public void markRead(Instant now) { if (readAt == null) readAt = now; }
    @PrePersist void prePersist() { var now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public String getTargetType() { return targetType; }
    public UUID getTargetId() { return targetId; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getDedupeKey() { return dedupeKey; }
    public Instant getReadAt() { return readAt; }
    public Instant getCreatedAt() { return createdAt; }
}
