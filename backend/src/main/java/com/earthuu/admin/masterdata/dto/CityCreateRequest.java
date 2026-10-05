package com.earthuu.admin.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CityCreateRequest(
        @NotBlank @Size(max = 30) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String code,
        @NotBlank @Size(max = 100) String officialName,
        @NotBlank @Size(max = 50) String displayNameKo,
        @Size(max = 50) String displayNameEn
) {}
