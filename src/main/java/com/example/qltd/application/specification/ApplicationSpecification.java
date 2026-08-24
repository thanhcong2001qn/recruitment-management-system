package com.example.qltd.application.specification;

import com.example.qltd.application.dto.request.ApplicationSearchRequest;
import com.example.qltd.application.entity.Application;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

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