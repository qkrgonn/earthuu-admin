package com.earthuu.admin.report.service;

import com.earthuu.admin.report.dto.ReportSearchRequest;
import com.earthuu.admin.report.entity.Report;
import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.entity.UserProfile;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

final class ReportSpecifications {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

    private ReportSpecifications() {}

    static Specification<Report> search(ReportSearchRequest request) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();

            if (request.getTargetType() != null) {
                predicates.add(builder.equal(root.get("targetType"), request.getTargetType()));
            }
            if (request.getStatus() != null) {
                predicates.add(builder.equal(root.get("status"), request.getStatus()));
            }
            if (request.getReportedFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"),
                        request.getReportedFrom().atStartOfDay(BUSINESS_ZONE).toInstant()));
            }
            if (request.getReportedTo() != null) {
                predicates.add(builder.lessThan(root.get("createdAt"),
                        request.getReportedTo().plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant()));
            }
            if (request.getQuery() != null && !request.getQuery().isBlank()) {
                var keyword = request.getQuery().trim();
                var like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";

                var eventName = query.subquery(Integer.class);
                var event = eventName.from(Event.class);
                var version = event.join("currentVersion", JoinType.LEFT);
                eventName.select(builder.literal(1)).where(
                        builder.equal(event.get("id"), root.get("targetId")),
                        builder.like(builder.lower(version.get("title")), like));

                var hostName = query.subquery(Integer.class);
                var profile = hostName.from(UserProfile.class);
                hostName.select(builder.literal(1)).where(
                        builder.equal(profile.get("userId"), root.get("targetId")),
                        builder.like(builder.lower(profile.get("name")), like));

                var matches = new ArrayList<Predicate>();
                matches.add(builder.like(builder.lower(root.get("reasonCode")), like));
                matches.add(builder.like(builder.lower(root.get("description")), like));
                matches.add(builder.exists(eventName));
                matches.add(builder.exists(hostName));
                try {
                    var id = UUID.fromString(keyword);
                    matches.add(builder.equal(root.get("id"), id));
                    matches.add(builder.equal(root.get("targetId"), id));
                } catch (IllegalArgumentException ignored) {
                    // 일반 검색어는 텍스트 필드와 대상 이름만 조회한다.
                }
                predicates.add(builder.or(matches.toArray(Predicate[]::new)));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
