package com.earthuu.admin.masterdata.dto;

import com.earthuu.admin.masterdata.entity.UniversityDomain;
import java.time.Instant;
import java.util.UUID;

public record DomainResponse(String domain, UUID universityId, UUID verifiedBy,
                             Instant verifiedAt, boolean active) {
    public static DomainResponse from(UniversityDomain item) {
        return new DomainResponse(item.getDomain(), item.getUniversityId(), item.getVerifiedBy(),
                item.getVerifiedAt(), item.isActive());
    }
}
