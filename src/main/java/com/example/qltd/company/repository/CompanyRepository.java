package com.example.qltd.company.repository;

import java.util.Optional;
import com.example.qltd.company.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompanyRepository extends JpaRepository<Company, Long>,
        JpaSpecificationExecutor<Company> {

    Optional<Company> findBySlug(String slug);

    Optional<Company> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByEmailIgnoreCase(String email);

    Page<Company> findByDeletedFalse(Pageable pageable);

    Optional<Company> findByIdAndDeletedFalse(Long id);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id);
}
