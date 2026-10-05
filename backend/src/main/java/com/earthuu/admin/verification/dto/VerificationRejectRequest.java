package com.earthuu.admin.verification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificationRejectRequest(@NotBlank @Size(max = 1000) String reason) {}
