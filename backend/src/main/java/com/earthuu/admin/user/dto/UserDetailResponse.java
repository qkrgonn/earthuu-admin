package com.earthuu.admin.user.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserDetailResponse(UUID id, String email, String name, String universityName,
                                 String status, String role, Instant createdAt, Instant updatedAt, int revision,
                                 List<UserRestrictionResponse> restrictions,
                                 List<UserStatusActionResponse> statusHistory) {
}
