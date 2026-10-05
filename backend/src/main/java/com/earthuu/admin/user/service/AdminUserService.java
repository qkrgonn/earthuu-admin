package com.earthuu.admin.user.service;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.UserStatus;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.auth.service.AdminAuthService;
import com.earthuu.admin.global.audit.AuditLogService;
import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import com.earthuu.admin.report.repository.UserProfileRepository;
import com.earthuu.admin.report.repository.UserRestrictionRepository;
import com.earthuu.admin.user.dto.*;
import com.earthuu.admin.user.entity.UserStatusAction;
import com.earthuu.admin.user.entity.UserStatusActionType;
import com.earthuu.admin.user.repository.UserStatusActionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminUserService {
    private final AdminUserRepository userRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final UserProfileRepository profileRepository;
    private final UserRestrictionRepository restrictionRepository;
    private final UserStatusActionRepository statusActionRepository;
    private final AdminAuthService adminAuthService;
    private final AuditLogService auditLogService;

    public AdminUserService(AdminUserRepository userRepository,
                            PasswordCredentialRepository credentialRepository,
                            UserProfileRepository profileRepository,
                            UserRestrictionRepository restrictionRepository,
                            UserStatusActionRepository statusActionRepository,
                            AdminAuthService adminAuthService,
                            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.profileRepository = profileRepository;
        this.restrictionRepository = restrictionRepository;
        this.statusActionRepository = statusActionRepository;
        this.adminAuthService = adminAuthService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public UserPageResponse<UserListResponse> search(UserSearchRequest request) {
        var direction = request.getSort() == UserSort.OLDEST ? Sort.Direction.ASC : Sort.Direction.DESC;
        var page = userRepository.findAll(UserSpecifications.search(request),
                        PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, "createdAt")))
                .map(this::toListResponse);
        return UserPageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getDetail(UUID userId) {
        var user = getUser(userId);
        var credential = credentialRepository.findByUserId(userId).orElse(null);
        var profile = profileRepository.findById(userId).orElse(null);
        var now = Instant.now();
        var restrictions = restrictionRepository.findByUserIdOrderByStartsAtDesc(userId).stream()
                .map(item -> UserRestrictionResponse.from(item, now)).toList();
        var history = statusActionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(UserStatusActionResponse::from).toList();
        return new UserDetailResponse(userId,
                credential == null ? null : credential.getLoginEmail(),
                profile == null ? null : profile.getName(),
                profile == null ? null : profile.getUniversityName(),
                user.getStatus().name(), user.getRole().name(), user.getCreatedAt(), user.getUpdatedAt(),
                user.getRevision(), restrictions, history);
    }

    @Transactional
    public UserActionResponse suspend(UUID userId, String reason, Authentication authentication) {
        validateReason(reason);
        var user = getUser(userId);
        var actorId = currentAdminId(authentication);
        var previous = user.getStatus();
        user.suspend();
        saveStatusAction(user, actorId, UserStatusActionType.SUSPENDED, previous, reason.trim());
        auditLogService.recordUserAction(actorId, "USER_SUSPENDED", userId,
                Map.of("previousStatus", previous.name(), "newStatus", user.getStatus().name(), "reason", reason.trim()));
        userRepository.flush();
        return UserActionResponse.from(user);
    }

    @Transactional
    public UserActionResponse restore(UUID userId, String reason, Authentication authentication) {
        validateReason(reason);
        var user = getUser(userId);
        var actorId = currentAdminId(authentication);
        var previous = user.getStatus();
        user.restore();
        saveStatusAction(user, actorId, UserStatusActionType.RESTORED, previous, reason.trim());
        auditLogService.recordUserAction(actorId, "USER_RESTORED", userId,
                Map.of("previousStatus", previous.name(), "newStatus", user.getStatus().name(), "reason", reason.trim()));
        userRepository.flush();
        return UserActionResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserRestrictionResponse> getRestrictions(UUID userId) {
        getUser(userId);
        var now = Instant.now();
        return restrictionRepository.findByUserIdOrderByStartsAtDesc(userId).stream()
                .map(item -> UserRestrictionResponse.from(item, now)).toList();
    }

    @Transactional
    public UserRestrictionResponse revokeRestriction(UUID userId, UUID restrictionId, String reason,
                                                      Authentication authentication) {
        validateReason(reason);
        getUser(userId);
        var restriction = restrictionRepository.findByIdAndUserId(restrictionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTRICTION_NOT_FOUND));
        var actorId = currentAdminId(authentication);
        var now = Instant.now();
        restriction.revoke(actorId, reason.trim(), now);
        auditLogService.recordUserAction(actorId, "USER_RESTRICTION_REVOKED", userId,
                Map.of("restrictionId", restrictionId, "reason", reason.trim()));
        restrictionRepository.flush();
        return UserRestrictionResponse.from(restriction, now);
    }

    private UserListResponse toListResponse(AdminUser user) {
        var credential = credentialRepository.findByUserId(user.getId()).orElse(null);
        var profile = profileRepository.findById(user.getId()).orElse(null);
        return UserListResponse.from(user,
                credential == null ? null : credential.getLoginEmail(),
                profile == null ? null : profile.getName(),
                profile == null ? null : profile.getUniversityName());
    }

    private void saveStatusAction(AdminUser user, UUID actorId, UserStatusActionType action,
                                  UserStatus previous, String reason) {
        statusActionRepository.save(UserStatusAction.record(
                user.getId(), actorId, action, previous, user.getStatus(), reason));
    }

    private AdminUser getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private UUID currentAdminId(Authentication authentication) {
        return adminAuthService.getAdminSession(authentication.getName()).id();
    }

    private void validateReason(String reason) {
        if (reason == null || reason.isBlank()) throw new BusinessException(ErrorCode.USER_ACTION_REASON_REQUIRED);
    }
}
