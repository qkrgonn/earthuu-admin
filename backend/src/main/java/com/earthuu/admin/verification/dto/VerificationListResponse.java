package com.earthuu.admin.verification.dto;

import com.earthuu.admin.verification.entity.StudentVerification;

import java.time.Instant;
import java.util.UUID;

public record VerificationListResponse(
        UUID id, UUID userId, String userName, String loginEmail, String schoolEmail,
        UUID universityId, String universityName, String status,
        Instant emailVerifiedAt, Instant createdAt, int revision
) {
    public static VerificationListResponse from(StudentVerification item, String userName,
                                                String loginEmail, String universityName) {
        return new VerificationListResponse(item.getId(), item.getUserId(), userName, loginEmail,
                item.getSchoolEmail(), item.getUniversityId(), universityName, item.getStatus().name(),
                item.getEmailVerifiedAt(), item.getCreatedAt(), item.getRevision());
    }
}
