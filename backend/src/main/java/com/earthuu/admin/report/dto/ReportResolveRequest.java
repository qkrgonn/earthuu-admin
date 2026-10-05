package com.earthuu.admin.report.dto;

import com.earthuu.admin.report.entity.ReportResolution;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportResolveRequest(
        @NotNull ReportResolution resolution,
        @NotBlank @Size(max = 2000) String reason
) {
}
