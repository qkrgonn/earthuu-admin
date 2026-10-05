package com.earthuu.admin.report.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record ReportPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> ReportPageResponse<T> from(Page<T> page) {
        return new ReportPageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
