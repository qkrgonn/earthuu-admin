package com.earthuu.admin.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UniversityRequest(@NotBlank @Size(max = 150) String nameKo,
                                @Size(max = 150) String nameEn) {}
