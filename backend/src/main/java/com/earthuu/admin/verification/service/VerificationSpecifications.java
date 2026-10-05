package com.earthuu.admin.verification.service;

import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.event.entity.UserProfile;
import com.earthuu.admin.verification.dto.VerificationSearchRequest;
import com.earthuu.admin.verification.entity.StudentVerification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

final class VerificationSpecifications {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private VerificationSpecifications() {}

    static Specification<StudentVerification> search(VerificationSearchRequest request) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (request.getStatus() != null) predicates.add(builder.equal(root.get("status"), request.getStatus()));
            if (request.getUniversityId() != null) {
                predicates.add(builder.equal(root.get("universityId"), request.getUniversityId()));
            }
            if (request.getSubmittedFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"),
                        request.getSubmittedFrom().atStartOfDay(KST).toInstant()));
            }
            if (request.getSubmittedTo() != null) {
                predicates.add(builder.lessThan(root.get("createdAt"),
                        request.getSubmittedTo().plusDays(1).atStartOfDay(KST).toInstant()));
            }
            if (request.getQuery() != null && !request.getQuery().isBlank()) {
                var keyword = "%" + request.getQuery().trim().toLowerCase() + "%";
                var matches = new ArrayList<Predicate>();
                matches.add(builder.like(builder.lower(root.get("schoolEmail")), keyword));

                var emailSubquery = query.subquery(UUID.class);
                var credential = emailSubquery.from(PasswordCredential.class);
                emailSubquery.select(credential.get("userId"))
                        .where(builder.like(builder.lower(credential.get("loginEmail")), keyword));
                matches.add(root.get("userId").in(emailSubquery));

                var profileSubquery = query.subquery(UUID.class);
                var profile = profileSubquery.from(UserProfile.class);
                profileSubquery.select(profile.get("userId"))
                        .where(builder.like(builder.lower(profile.get("name")), keyword));
                matches.add(root.get("userId").in(profileSubquery));

                try {
                    var id = UUID.fromString(request.getQuery().trim());
                    matches.add(builder.equal(root.get("id"), id));
                    matches.add(builder.equal(root.get("userId"), id));
                } catch (IllegalArgumentException ignored) {
                    // UUID가 아니면 문자열 필드만 검색한다.
                }
                predicates.add(builder.or(matches.toArray(Predicate[]::new)));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
