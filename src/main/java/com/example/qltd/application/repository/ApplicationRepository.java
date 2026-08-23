package com.example.qltd.application.repository;

import com.example.qltd.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    boolean existsByJobIdAndCandidateId(
            Long jobId,
            Long candidateId);

    Optional<Application> findByIdAndCandidateId(
            Long id,
            Long candidateId);
}