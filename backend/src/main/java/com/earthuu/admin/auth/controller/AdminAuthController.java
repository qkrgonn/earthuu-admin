package com.earthuu.admin.auth.controller;

import com.earthuu.admin.auth.dto.AdminSessionResponse;
import com.earthuu.admin.auth.dto.CsrfTokenResponse;
import com.earthuu.admin.auth.dto.LoginRequest;
import com.earthuu.admin.auth.service.AdminAuthService;
import com.earthuu.admin.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Authentication", description = "관리자 세션 인증")
@RestController
@RequestMapping("/api/admin/v1/auth")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AdminAuthService adminAuthService;

    public AdminAuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            AdminAuthService adminAuthService
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.adminAuthService = adminAuthService;
    }

    @Operation(summary = "CSRF 토큰 발급")
    @GetMapping("/csrf")
    public ApiResponse<CsrfTokenResponse> csrf(CsrfToken token) {
        return ApiResponse.of(new CsrfTokenResponse(token.getHeaderName(), token.getParameterName(), token.getToken()));
    }

    @Operation(summary = "관리자 로그인", description = "먼저 CSRF 토큰 발급 API를 호출해야 합니다.")
    @SecurityRequirement(name = "csrfToken")
    @PostMapping("/login")
    public ApiResponse<AdminSessionResponse> login(
            @Valid @RequestBody LoginRequest requestBody,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(requestBody.email(), requestBody.password())
        );
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        request.getSession(true);
        request.changeSessionId();
        securityContextRepository.saveContext(context, request, response);
        return ApiResponse.of(adminAuthService.getAdminSession(authentication.getName()));
    }

    @Operation(summary = "현재 관리자 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/me")
    public ApiResponse<AdminSessionResponse> me(Authentication authentication) {
        return ApiResponse.of(adminAuthService.getAdminSession(authentication.getName()));
    }

    @Operation(summary = "관리자 로그아웃")
    @SecurityRequirement(name = "sessionAuth")
    @SecurityRequirement(name = "csrfToken")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }
}
