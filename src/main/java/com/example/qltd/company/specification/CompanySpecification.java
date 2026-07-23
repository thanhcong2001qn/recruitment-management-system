package com.example.qltd.company.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import com.example.qltd.company.dto.request.CompanySearchRequest;
import com.example.qltd.company.entity.Company;
import java.util.ArrayList;
import java.util.List;

public final class CompanySpecification {

    private CompanySpecification() {
    }

    public static Specification<Company> search(CompanySearchRequest request) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy dữ liệu chưa bị xóa
            predicates.add(cb.isFalse(root.get("deleted")));

            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {

                String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("name")), keyword),
                                cb.like(cb.lower(root.get("description")), keyword)));
            }

            if (request.getCity() != null && !request.getCity().isBlank()) {

                predicates.add(
                        cb.equal(
                                cb.lower(root.get("city")),
                                request.getCity().trim().toLowerCase()));
            }

            if (request.getStatus() != null) {

                predicates.add(
                        cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getCompanySize() != null) {

                predicates.add(
                        cb.equal(root.get("companySize"), request.getCompanySize()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));

        };

    }

}
