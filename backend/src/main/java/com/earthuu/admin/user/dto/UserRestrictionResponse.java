package com.earthuu.admin.user.dto;

import com.earthuu.admin.report.entity.UserRestriction;
import java.time.Instant;
import java.util.UUID;

public record UserRestrictionResponse(UUID id, UUID sourceReportId, UUID imposedBy, String reason,
                                      String status, Instant startsAt, Instant endsAt, Instant revokedAt,
                                      UUID revokedBy, String revokeReason, int revision) {
    public static UserRestrictionResponse from(UserRestriction restriction, Instant now) {
        return new UserRestrictionResponse(restriction.getId(), restriction.getSourceReportId(),
                restriction.getImposedBy(), restriction.getReason(), restriction.effectiveStatusAt(now).name(),
                restriction.getStartsAt(), restriction.getEndsAt(), restriction.getRevokedAt(),
                restriction.getRevokedBy(), restriction.getRevokeReason(), restriction.getRevision());
    }
}
