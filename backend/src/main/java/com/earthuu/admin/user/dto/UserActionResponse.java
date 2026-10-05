package com.earthuu.admin.user.dto;

import com.earthuu.admin.auth.entity.AdminUser;
import java.util.UUID;

public record UserActionResponse(UUID userId, String status, int revision) {
    public static UserActionResponse from(AdminUser user) {
        return new UserActionResponse(user.getId(), user.getStatus().name(), user.getRevision());
    }
}
