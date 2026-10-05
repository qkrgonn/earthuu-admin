package com.earthuu.admin.notification.dto;

import com.earthuu.admin.notification.entity.NotificationSetting;
import java.time.Instant;

public record NotificationSettingResponse(boolean pushEnabled, boolean chatEnabled, Instant updatedAt) {
    public static NotificationSettingResponse from(NotificationSetting item) {
        return new NotificationSettingResponse(item.isPushEnabled(), item.isChatEnabled(), item.getUpdatedAt());
    }
}
