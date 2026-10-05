package com.earthuu.admin.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserActionRequest(
        @NotBlank(message = "처리 사유는 필수입니다.")
        @Size(max = 1000, message = "처리 사유는 1000자 이하여야 합니다.") String reason) {
}
