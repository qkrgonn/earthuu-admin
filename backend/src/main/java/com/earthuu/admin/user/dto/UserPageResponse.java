package com.earthuu.admin.user.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record UserPageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> UserPageResponse<T> from(Page<T> page) {
        return new UserPageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
