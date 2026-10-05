package com.earthuu.admin.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(@NotBlank @Size(max = 80) String nameKo,
                                    @Size(max = 80) String nameEn) {}
