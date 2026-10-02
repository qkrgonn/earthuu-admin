package com.earthuu.admin.auth.dto;

public record CsrfTokenResponse(String headerName, String parameterName, String token) {
}
