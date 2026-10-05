package com.earthuu.admin.masterdata.dto;

import com.earthuu.admin.masterdata.entity.City;

public record CityResponse(String code, String officialName, String displayNameKo,
                           String displayNameEn, boolean active) {
    public static CityResponse from(City item) {
        return new CityResponse(item.getCode(), item.getOfficialName(), item.getDisplayNameKo(),
                item.getDisplayNameEn(), item.isActive());
    }
}
