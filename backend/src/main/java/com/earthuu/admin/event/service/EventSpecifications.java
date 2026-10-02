package com.earthuu.admin.event.service;

import com.earthuu.admin.event.dto.EventSearchRequest;
import com.earthuu.admin.event.dto.EventSort;
import com.earthuu.admin.event.entity.Event;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

final class EventSpecifications {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

    private EventSpecifications() {
    }

    static Specification<Event> search(EventSearchRequest request) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            var version = root.join("currentVersion", JoinType.INNER);
            var profile = root.join("hostProfile", JoinType.LEFT);

            if (request.getModerationStatus() != null) {
                predicates.add(builder.equal(root.get("moderationStatus"), request.getModerationStatus()));
            }
            if (request.getLifecycleStatus() != null) {
                predicates.add(builder.equal(root.get("lifecycleStatus"), request.getLifecycleStatus()));
            }
            if (request.getSubmittedFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(version.get("submittedAt"),
                        request.getSubmittedFrom().atStartOfDay(BUSINESS_ZONE).toInstant()));
            }
            if (request.getSubmittedTo() != null) {
                predicates.add(builder.lessThan(version.get("submittedAt"),
                        request.getSubmittedTo().plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant()));
            }
            if (request.getQuery() != null && !request.getQuery().isBlank()) {
                var keyword = request.getQuery().trim();
                var like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                var textMatch = builder.or(
                        builder.like(builder.lower(version.get("title")), like),
                        builder.like(builder.lower(profile.get("name")), like)
                );
                try {
                    var id = UUID.fromString(keyword);
                    predicates.add(builder.or(textMatch, builder.equal(root.get("id"), id), builder.equal(root.get("hostId"), id)));
                } catch (IllegalArgumentException ignored) {
                    predicates.add(textMatch);
                }
            }

            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                var sort = request.getSort() == null ? EventSort.OLDEST_SUBMITTED : request.getSort();
                switch (sort) {
                    case NEWEST_SUBMITTED -> query.orderBy(builder.desc(version.get("submittedAt")), builder.desc(root.get("createdAt")));
                    case STARTING_SOON -> query.orderBy(builder.asc(version.get("startsAt")), builder.asc(root.get("createdAt")));
                    case OLDEST_SUBMITTED -> query.orderBy(builder.asc(version.get("submittedAt")), builder.asc(root.get("createdAt")));
                }
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
