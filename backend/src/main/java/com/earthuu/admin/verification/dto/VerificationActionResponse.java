package com.earthuu.admin.verification.dto;

import com.earthuu.admin.verification.entity.StudentVerification;

import java.time.Instant;
import java.util.UUID;

public record VerificationActionResponse(UUID id, String status, UUID reviewedBy,
                                         Instant reviewedAt, String rejectionReason, int revision) {
    public static VerificationActionResponse from(StudentVerification item) {
        return new VerificationActionResponse(item.getId(), item.getStatus().name(), item.getReviewedBy(),
                item.getReviewedAt(), item.getRejectionReason(), item.getRevision());
    }
}
