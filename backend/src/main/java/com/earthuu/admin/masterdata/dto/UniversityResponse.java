package com.earthuu.admin.masterdata.dto;

import com.earthuu.admin.event.entity.University;
import java.util.UUID;

public record UniversityResponse(UUID id, String nameKo, String nameEn, boolean active) {
    public static UniversityResponse from(University item) {
        return new UniversityResponse(item.getId(), item.getNameKo(), item.getNameEn(), item.isActive());
    }
}
