package com.earthuu.admin.auth.service;

import com.earthuu.admin.auth.dto.AdminSessionResponse;
import com.earthuu.admin.auth.entity.UserRole;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {

    private final PasswordCredentialRepository credentialRepository;
    private final AdminUserRepository userRepository;

    public AdminAuthService(
            PasswordCredentialRepository credentialRepository,
            AdminUserRepository userRepository
    ) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AdminSessionResponse getAdminSession(String email) {
        var credential = credentialRepository.findByLoginEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        var user = userRepository.findById(credential.getUserId())
                .filter(found -> found.getRole() == UserRole.ADMIN)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        return new AdminSessionResponse(user.getId(), credential.getLoginEmail(), user.getRole().name());
    }
}
