package com.earthuu.admin.user.dto;

import com.earthuu.admin.auth.entity.AdminUser;
import java.time.Instant;
import java.util.UUID;

public record UserListResponse(UUID id, String email, String name, String universityName,
                               String status, String role, Instant createdAt, int revision) {
    public static UserListResponse from(AdminUser user, String email, String name, String universityName) {
        return new UserListResponse(user.getId(), email, name, universityName, user.getStatus().name(),
                user.getRole().name(), user.getCreatedAt(), user.getRevision());
    }
}
