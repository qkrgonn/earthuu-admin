package com.earthuu.admin.user.controller;

import com.earthuu.admin.global.response.ApiResponse;
import com.earthuu.admin.user.dto.*;
import com.earthuu.admin.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/admin/v1/users")
@Tag(name = "Admin User Management", description = "관리자 사용자 조회 및 계정·활동 제한 관리 API")
@SecurityRequirement(name = "sessionAuth")
public class AdminUserController {
    private final AdminUserService service;

    public AdminUserController(AdminUserService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "사용자 목록 조회")
    public ApiResponse<UserPageResponse<UserListResponse>> search(
            @Valid @ParameterObject UserSearchRequest request) {
        return ApiResponse.of(service.search(request));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "사용자 상세·계정 처리 이력·활동 제한 조회")
    public ApiResponse<UserDetailResponse> getDetail(@PathVariable UUID userId) {
        return ApiResponse.of(service.getDetail(userId));
    }

    @PostMapping("/{userId}/suspend")
    @Operation(summary = "사용자 계정 정지")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<UserActionResponse> suspend(@PathVariable UUID userId,
                                                   @Valid @RequestBody UserActionRequest request,
                                                   Authentication authentication) {
        return ApiResponse.of(service.suspend(userId, request.reason(), authentication));
    }

    @PostMapping("/{userId}/restore")
    @Operation(summary = "정지 사용자 계정 복구")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<UserActionResponse> restore(@PathVariable UUID userId,
                                                   @Valid @RequestBody UserActionRequest request,
                                                   Authentication authentication) {
        return ApiResponse.of(service.restore(userId, request.reason(), authentication));
    }

    @GetMapping("/{userId}/restrictions")
    @Operation(summary = "사용자 활동 제한 목록 조회")
    public ApiResponse<List<UserRestrictionResponse>> getRestrictions(@PathVariable UUID userId) {
        return ApiResponse.of(service.getRestrictions(userId));
    }

    @PostMapping("/{userId}/restrictions/{restrictionId}/revoke")
    @Operation(summary = "사용자 활동 제한 해제")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<UserRestrictionResponse> revokeRestriction(
            @PathVariable UUID userId,
            @PathVariable UUID restrictionId,
            @Valid @RequestBody UserActionRequest request,
            Authentication authentication) {
        return ApiResponse.of(service.revokeRestriction(userId, restrictionId, request.reason(), authentication));
    }
}
