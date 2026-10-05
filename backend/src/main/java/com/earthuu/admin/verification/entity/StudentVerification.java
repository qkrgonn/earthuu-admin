package com.earthuu.admin.verification.entity;

import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_verifications")
public class StudentVerification {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "school_email", nullable = false, length = 320) private String schoolEmail;
    @Column(name = "university_id", nullable = false) private UUID universityId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private VerificationStatus status;
    @Column(name = "email_verified_at") private Instant emailVerifiedAt;
    @Column(name = "domain_verified_by") private UUID domainVerifiedBy;
    @Column(name = "verified_at") private Instant verifiedAt;
    @Column(name = "reviewed_by") private UUID reviewedBy;
    @Column(name = "reviewed_at") private Instant reviewedAt;
    @Column(name = "rejection_reason", columnDefinition = "text") private String rejectionReason;
    @Version @Column(nullable = false) private int revision;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected StudentVerification() {}

    private StudentVerification(UUID userId, String schoolEmail, UUID universityId,
                                Instant emailVerifiedAt, UUID domainVerifiedBy) {
        this.userId = userId;
        this.schoolEmail = schoolEmail.toLowerCase();
        this.universityId = universityId;
        this.emailVerifiedAt = emailVerifiedAt;
        this.domainVerifiedBy = domainVerifiedBy;
        this.status = VerificationStatus.PENDING;
    }

    public static StudentVerification pending(UUID userId, String schoolEmail, UUID universityId,
                                              Instant emailVerifiedAt, UUID domainVerifiedBy) {
        return new StudentVerification(userId, schoolEmail, universityId, emailVerifiedAt, domainVerifiedBy);
    }

    public void approve(UUID reviewerId, Instant now) {
        requirePending();
        if (emailVerifiedAt == null) throw new BusinessException(ErrorCode.VERIFICATION_EMAIL_NOT_VERIFIED);
        if (domainVerifiedBy == null) throw new BusinessException(ErrorCode.VERIFICATION_DOMAIN_NOT_VERIFIED);
        status = VerificationStatus.APPROVED;
        reviewedBy = reviewerId;
        reviewedAt = now;
        verifiedAt = now;
        rejectionReason = null;
    }

    public void reject(UUID reviewerId, String reason, Instant now) {
        requirePending();
        status = VerificationStatus.REJECTED;
        reviewedBy = reviewerId;
        reviewedAt = now;
        rejectionReason = reason;
    }

    private void requirePending() {
        if (status != VerificationStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_STATUS);
        }
    }

    @PrePersist void prePersist() { var now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getSchoolEmail() { return schoolEmail; }
    public UUID getUniversityId() { return universityId; }
    public VerificationStatus getStatus() { return status; }
    public Instant getEmailVerifiedAt() { return emailVerifiedAt; }
    public UUID getDomainVerifiedBy() { return domainVerifiedBy; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public UUID getReviewedBy() { return reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public int getRevision() { return revision; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
