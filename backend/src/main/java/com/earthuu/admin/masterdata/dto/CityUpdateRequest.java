package com.earthuu.admin.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CityUpdateRequest(@NotBlank @Size(max = 100) String officialName,
                                @NotBlank @Size(max = 50) String displayNameKo,
                                @Size(max = 50) String displayNameEn) {}
