package com.earthuu.admin.verification.dto;

import com.earthuu.admin.verification.entity.StudentVerification;

import java.time.Instant;
import java.util.UUID;

public record VerificationDetailResponse(
        UUID id, UUID userId, String userName, String loginEmail, String schoolEmail,
        UUID universityId, String universityName, String status,
        Instant emailVerifiedAt, UUID domainVerifiedBy, Instant verifiedAt,
        UUID reviewedBy, Instant reviewedAt, String rejectionReason,
        Instant createdAt, Instant updatedAt, int revision
) {
    public static VerificationDetailResponse from(StudentVerification item, String userName,
                                                  String loginEmail, String universityName) {
        return new VerificationDetailResponse(item.getId(), item.getUserId(), userName, loginEmail,
                item.getSchoolEmail(), item.getUniversityId(), universityName, item.getStatus().name(),
                item.getEmailVerifiedAt(), item.getDomainVerifiedBy(), item.getVerifiedAt(), item.getReviewedBy(),
                item.getReviewedAt(), item.getRejectionReason(), item.getCreatedAt(), item.getUpdatedAt(),
                item.getRevision());
    }
}
