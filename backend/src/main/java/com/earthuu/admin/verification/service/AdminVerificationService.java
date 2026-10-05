package com.earthuu.admin.verification.service;

import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.auth.service.AdminAuthService;
import com.earthuu.admin.event.dto.PageResponse;
import com.earthuu.admin.global.audit.AuditLogService;
import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import com.earthuu.admin.report.repository.UserProfileRepository;
import com.earthuu.admin.notification.service.OutboxPublisher;
import com.earthuu.admin.verification.dto.*;
import com.earthuu.admin.verification.entity.StudentVerification;
import com.earthuu.admin.verification.repository.StudentVerificationRepository;
import com.earthuu.admin.verification.repository.VerificationUniversityRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminVerificationService {
    private final StudentVerificationRepository verificationRepository;
    private final VerificationUniversityRepository universityRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final UserProfileRepository profileRepository;
    private final AdminAuthService adminAuthService;
    private final AuditLogService auditLogService;
    private final OutboxPublisher outboxPublisher;

    public AdminVerificationService(StudentVerificationRepository verificationRepository,
                                    VerificationUniversityRepository universityRepository,
                                    PasswordCredentialRepository credentialRepository,
                                    UserProfileRepository profileRepository,
                                    AdminAuthService adminAuthService,
                                    AuditLogService auditLogService,
                                    OutboxPublisher outboxPublisher) {
        this.verificationRepository = verificationRepository;
        this.universityRepository = universityRepository;
        this.credentialRepository = credentialRepository;
        this.profileRepository = profileRepository;
        this.adminAuthService = adminAuthService;
        this.auditLogService = auditLogService;
        this.outboxPublisher = outboxPublisher;
    }

    @Transactional(readOnly = true)
    public PageResponse<VerificationListResponse> search(VerificationSearchRequest request) {
        validateDateRange(request);
        var direction = request.getSort() == VerificationSort.OLDEST ? Sort.Direction.ASC : Sort.Direction.DESC;
        var page = verificationRepository.findAll(VerificationSpecifications.search(request),
                        PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, "createdAt")))
                .map(this::toListResponse);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public VerificationDetailResponse getDetail(UUID verificationId) {
        return toDetailResponse(getVerification(verificationId));
    }

    @Transactional
    public VerificationActionResponse approve(UUID verificationId, Authentication authentication) {
        var verification = getVerification(verificationId);
        var actorId = currentAdminId(authentication);
        verification.approve(actorId, Instant.now());
        auditLogService.recordVerificationAction(actorId, "STUDENT_VERIFICATION_APPROVED", verificationId,
                Map.of("userId", verification.getUserId(), "universityId", verification.getUniversityId()));
        outboxPublisher.enqueueNotification(verification.getUserId(), "STUDENT_VERIFICATION_APPROVED",
                "STUDENT_VERIFICATION", verificationId, "학생 인증이 승인되었습니다.",
                "학교 이메일과 학생 정보 확인이 완료되었습니다.", "verification-approved:" + verificationId);
        verificationRepository.flush();
        return VerificationActionResponse.from(verification);
    }

    @Transactional
    public VerificationActionResponse reject(UUID verificationId, String reason, Authentication authentication) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VERIFICATION_REJECT_REASON_REQUIRED);
        }
        var verification = getVerification(verificationId);
        var actorId = currentAdminId(authentication);
        verification.reject(actorId, reason.trim(), Instant.now());
        auditLogService.recordVerificationAction(actorId, "STUDENT_VERIFICATION_REJECTED", verificationId,
                Map.of("userId", verification.getUserId(), "reason", reason.trim()));
        outboxPublisher.enqueueNotification(verification.getUserId(), "STUDENT_VERIFICATION_REJECTED",
                "STUDENT_VERIFICATION", verificationId, "학생 인증이 반려되었습니다.",
                "학생 인증 신청 결과와 반려 사유를 확인해 주세요.", "verification-rejected:" + verificationId);
        verificationRepository.flush();
        return VerificationActionResponse.from(verification);
    }

    private VerificationListResponse toListResponse(StudentVerification item) {
        var credential = credentialRepository.findByUserId(item.getUserId()).orElse(null);
        var profile = profileRepository.findById(item.getUserId()).orElse(null);
        var university = universityRepository.findById(item.getUniversityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIVERSITY_NOT_FOUND));
        return VerificationListResponse.from(item, profile == null ? null : profile.getName(),
                credential == null ? null : credential.getLoginEmail(), university.getNameKo());
    }

    private VerificationDetailResponse toDetailResponse(StudentVerification item) {
        var credential = credentialRepository.findByUserId(item.getUserId()).orElse(null);
        var profile = profileRepository.findById(item.getUserId()).orElse(null);
        var university = universityRepository.findById(item.getUniversityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIVERSITY_NOT_FOUND));
        return VerificationDetailResponse.from(item, profile == null ? null : profile.getName(),
                credential == null ? null : credential.getLoginEmail(), university.getNameKo());
    }

    private StudentVerification getVerification(UUID verificationId) {
        return verificationRepository.findById(verificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND));
    }

    private UUID currentAdminId(Authentication authentication) {
        return adminAuthService.getAdminSession(authentication.getName()).id();
    }

    private void validateDateRange(VerificationSearchRequest request) {
        if (request.getSubmittedFrom() != null && request.getSubmittedTo() != null
                && request.getSubmittedFrom().isAfter(request.getSubmittedTo())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_DATE_RANGE);
        }
    }
}
