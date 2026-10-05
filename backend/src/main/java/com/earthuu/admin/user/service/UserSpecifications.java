package com.earthuu.admin.user.service;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.event.entity.UserProfile;
import com.earthuu.admin.user.dto.UserSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.UUID;

final class UserSpecifications {
    private UserSpecifications() {}

    static Specification<AdminUser> search(UserSearchRequest request) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (request.getStatus() != null) predicates.add(builder.equal(root.get("status"), request.getStatus()));
            if (request.getRole() != null) predicates.add(builder.equal(root.get("role"), request.getRole()));

            if (request.getQuery() != null && !request.getQuery().isBlank()) {
                var keyword = "%" + request.getQuery().trim().toLowerCase() + "%";
                var emailSubquery = query.subquery(UUID.class);
                var credential = emailSubquery.from(PasswordCredential.class);
                emailSubquery.select(credential.get("userId"))
                        .where(builder.like(builder.lower(credential.get("loginEmail")), keyword));

                var profileSubquery = query.subquery(UUID.class);
                var profile = profileSubquery.from(UserProfile.class);
                profileSubquery.select(profile.get("userId"))
                        .where(builder.like(builder.lower(profile.get("name")), keyword));

                var matches = new ArrayList<Predicate>();
                matches.add(root.get("id").in(emailSubquery));
                matches.add(root.get("id").in(profileSubquery));
                try {
                    matches.add(builder.equal(root.get("id"), UUID.fromString(request.getQuery().trim())));
                } catch (IllegalArgumentException ignored) {
                    // UUID가 아닌 검색어는 이메일과 이름으로만 검색한다.
                }
                predicates.add(builder.or(matches.toArray(Predicate[]::new)));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
