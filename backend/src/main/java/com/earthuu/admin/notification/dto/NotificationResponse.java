package com.earthuu.admin.notification.dto;

import com.earthuu.admin.notification.entity.Notification;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, String type, String targetType, UUID targetId,
                                   String title, String body, Instant readAt, Instant createdAt) {
    public static NotificationResponse from(Notification item) {
        return new NotificationResponse(item.getId(), item.getType(), item.getTargetType(), item.getTargetId(),
                item.getTitle(), item.getBody(), item.getReadAt(), item.getCreatedAt());
    }
}
