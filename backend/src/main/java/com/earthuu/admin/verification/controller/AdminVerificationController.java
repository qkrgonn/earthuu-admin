package com.earthuu.admin.verification.controller;

import com.earthuu.admin.event.dto.PageResponse;
import com.earthuu.admin.global.response.ApiResponse;
import com.earthuu.admin.verification.dto.*;
import com.earthuu.admin.verification.service.AdminVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/admin/v1/verifications")
@Tag(name = "Student Verification Management", description = "학생 인증 신청 조회 및 승인·반려 API")
@SecurityRequirement(name = "sessionAuth")
public class AdminVerificationController {
    private final AdminVerificationService service;

    public AdminVerificationController(AdminVerificationService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "학생 인증 신청 목록 조회")
    public ApiResponse<PageResponse<VerificationListResponse>> search(
            @Valid @ParameterObject VerificationSearchRequest request) {
        return ApiResponse.of(service.search(request));
    }

    @GetMapping("/{verificationId}")
    @Operation(summary = "학생 인증 신청 상세 조회")
    public ApiResponse<VerificationDetailResponse> getDetail(@PathVariable UUID verificationId) {
        return ApiResponse.of(service.getDetail(verificationId));
    }

    @PostMapping("/{verificationId}/approve")
    @Operation(summary = "학생 인증 승인")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<VerificationActionResponse> approve(@PathVariable UUID verificationId,
                                                           Authentication authentication) {
        return ApiResponse.of(service.approve(verificationId, authentication));
    }

    @PostMapping("/{verificationId}/reject")
    @Operation(summary = "학생 인증 반려")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<VerificationActionResponse> reject(@PathVariable UUID verificationId,
                                                          @Valid @RequestBody VerificationRejectRequest request,
                                                          Authentication authentication) {
        return ApiResponse.of(service.reject(verificationId, request.reason(), authentication));
    }
}
