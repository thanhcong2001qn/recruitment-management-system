package com.example.qltd.job.specification;

import com.example.qltd.job.dto.request.JobSearchRequest;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class JobSpecification {

    private JobSpecification() {
    }

    public static Specification<Job> search(
            JobSearchRequest request,
            boolean publicSearch) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy Job chưa bị soft delete.
            predicates.add(
                    cb.isFalse(root.get("deleted")));

            if (publicSearch) {

                predicates.add(
                        cb.equal(
                                root.get("status"),
                                JobStatus.PUBLISHED));
            }

            // Keyword: title hoặc description.
            if (request.getKeyword() != null
                    && !request.getKeyword().isBlank()) {

                String keyword = "%" + request.getKeyword()
                        .trim()
                        .toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(
                                        cb.lower(
                                                root.get("title")),
                                        keyword),
                                cb.like(
                                        cb.lower(
                                                root.get("description")),
                                        keyword)));
            }

            if (request.getCompanyId() != null) {

                predicates.add(
                        cb.equal(
                                root.get("company").get("id"),
                                request.getCompanyId()));
            }

            if (request.getLocation() != null
                    && !request.getLocation().isBlank()) {

                predicates.add(
                        cb.equal(
                                cb.lower(
                                        root.get("location")),
                                request.getLocation()
                                        .trim()
                                        .toLowerCase()));
            }

            if (request.getStatus() != null) {

                predicates.add(
                        cb.equal(
                                root.get("status"),
                                request.getStatus()));
            }

            if (request.getWorkingType() != null) {

                predicates.add(
                        cb.equal(
                                root.get("workingType"),
                                request.getWorkingType()));
            }

            if (request.getExperienceLevel() != null) {

                predicates.add(
                        cb.equal(
                                root.get("experienceLevel"),
                                request.getExperienceLevel()));
            }

            if (request.getEmploymentType() != null) {

                predicates.add(
                        cb.equal(
                                root.get("employmentType"),
                                request.getEmploymentType()));
            }

            return cb.and(
                    predicates.toArray(
                            new Predicate[0]));
        };
    }
}