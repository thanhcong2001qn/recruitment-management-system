package com.example.qltd.interview.repository;

import com.example.qltd.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    boolean existsByApplicationIdAndRoundNumber(
            Long applicationId,
            Integer roundNumber);

    Optional<Interview> findByApplicationIdAndRoundNumber(
            Long applicationId,
            Integer roundNumber);

    List<Interview> findAllByApplicationIdOrderByRoundNumberAsc(
            Long applicationId);
}
