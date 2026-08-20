package com.example.qltd.job.repository;

import com.example.qltd.job.entity.Job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface JobRepository
        extends JpaRepository<Job, Long>,
        JpaSpecificationExecutor<Job> {

    Optional<Job> findByIdAndDeletedFalse(Long id);

    Optional<Job> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByTitleIgnoreCaseAndCompanyId(
            String title,
            Long companyId);

    boolean existsByTitleIgnoreCaseAndCompanyIdAndIdNot(
            String title,
            Long companyId,
            Long id);
}