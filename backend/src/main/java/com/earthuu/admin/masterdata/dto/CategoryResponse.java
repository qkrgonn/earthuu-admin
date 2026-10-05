package com.earthuu.admin.masterdata.dto;

import com.earthuu.admin.masterdata.entity.Category;
import java.util.UUID;

public record CategoryResponse(UUID id, String code, String nameKo, String nameEn, boolean active) {
    public static CategoryResponse from(Category item) {
        return new CategoryResponse(item.getId(), item.getCode(), item.getNameKo(), item.getNameEn(), item.isActive());
    }
}
