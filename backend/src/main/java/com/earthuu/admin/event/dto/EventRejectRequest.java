package com.earthuu.admin.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EventRejectRequest(
        @NotBlank(message = "반려 사유는 필수입니다.")
        @Size(max = 500, message = "반려 사유는 500자 이하여야 합니다.")
        String reason
) {
}
