package com.earthuu.admin.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DomainRequest(
        @NotBlank @Size(max = 255)
        @Pattern(regexp = "^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "올바른 도메인 형식이어야 합니다.")
        String domain
) {}
