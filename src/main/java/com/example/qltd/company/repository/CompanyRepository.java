package com.example.qltd.company.repository;

import java.util.Optional;
import com.example.qltd.company.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository <Company, Long> {

    Optional<Company> findBySlug(String slug);

    Optional<Company> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    Page<Company> findByDeletedFalse(Pageable pageable);

    Optional<Company> findByIdAndDeletedFalse(Long id);
    
}
