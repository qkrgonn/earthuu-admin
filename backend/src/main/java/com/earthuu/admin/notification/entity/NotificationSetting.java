package com.earthuu.admin.notification.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_settings")
public class NotificationSetting {
    @Id @Column(name = "user_id") private UUID userId;
    @Column(name = "push_enabled", nullable = false) private boolean pushEnabled;
    @Column(name = "chat_enabled", nullable = false) private boolean chatEnabled;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected NotificationSetting() {}
    private NotificationSetting(UUID userId) { this.userId = userId; this.pushEnabled = true; this.chatEnabled = true; }
    public static NotificationSetting defaults(UUID userId) { return new NotificationSetting(userId); }
    public void update(boolean pushEnabled, boolean chatEnabled) { this.pushEnabled = pushEnabled; this.chatEnabled = chatEnabled; }
    @PrePersist @PreUpdate void touch() { updatedAt = Instant.now(); }
    public UUID getUserId() { return userId; }
    public boolean isPushEnabled() { return pushEnabled; }
    public boolean isChatEnabled() { return chatEnabled; }
    public Instant getUpdatedAt() { return updatedAt; }
}
