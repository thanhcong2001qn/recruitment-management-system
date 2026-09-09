package com.example.qltd.application.specification;

import com.example.qltd.application.dto.request.ApplicationSearchRequest;
import com.example.qltd.application.entity.Application;
import com.example.qltd.user.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ApplicationSpecification {

    private ApplicationSpecification() {
    }

    public static Specification<Application> search(
            Long jobId,
            ApplicationSearchRequest request) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    cb.equal(
                            root.get("job").get("id"),
                            jobId));

            if (request != null
                    && request.getKeyword() != null
                    && !request.getKeyword().isBlank()) {

                String keywordPattern = "%"
                        + request.getKeyword()
                                .trim()
                                .toLowerCase(Locale.ROOT)
                        + "%";

                Join<Application, User> candidate = root.join(
                        "candidate",
                        JoinType.INNER);

                Predicate candidateNameMatches = cb.like(
                        cb.lower(candidate.get("fullName")),
                        keywordPattern);

                Predicate candidateEmailMatches = cb.like(
                        cb.lower(candidate.get("email")),
                        keywordPattern);

                predicates.add(
                        cb.or(
                                candidateNameMatches,
                                candidateEmailMatches));
            }

            if (request != null
                    && request.getStatus() != null) {

                predicates.add(
                        cb.equal(
                                root.get("status"),
                                request.getStatus()));
            }

            return cb.and(
                    predicates.toArray(
                            new Predicate[0]));
        };
    }
}