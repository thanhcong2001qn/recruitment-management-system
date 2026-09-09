package com.example.qltd.application.repository;

import com.example.qltd.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ApplicationRepository
        extends JpaRepository<Application, Long>,
        JpaSpecificationExecutor<Application> {

    boolean existsByJobIdAndCandidateId(
            Long jobId,
            Long candidateId);

    Optional<Application> findByIdAndCandidateId(
            Long id,
            Long candidateId);
}