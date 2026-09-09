package com.example.qltd.interview.repository;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.support.InterviewTestFactory;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Interview Repository Integration Test")
class InterviewRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    private Application application;

    @BeforeEach
    void setUp() {

        interviewRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();

        Company company = companyRepository.save(
                ApplicationTestFactory.company());

        User candidate = userRepository.save(
                ApplicationTestFactory.candidate(
                        "interview-candidate@test.com"));

        Job job = jobRepository.save(
                ApplicationTestFactory.publishedJob(company));

        application = applicationRepository.save(
                ApplicationTestFactory.application(
                        job,
                        candidate));
    }

    @Test
    @DisplayName("Should find application interviews ordered by round number")
    void shouldFindInterviewsOrderedByRoundNumber() {

        LocalDateTime firstSchedule = LocalDateTime.of(
                2026,
                9,
                10,
                9,
                0);

        Interview secondRound = InterviewTestFactory.interview(
                application,
                2,
                firstSchedule.plusDays(2));

        Interview firstRound = InterviewTestFactory.interview(
                application,
                1,
                firstSchedule);

        interviewRepository.saveAllAndFlush(
                List.of(secondRound, firstRound));

        List<Interview> result = interviewRepository
                .findAllByApplicationIdOrderByRoundNumberAsc(
                        application.getId());

        assertThat(result)
                .extracting(Interview::getRoundNumber)
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("Database should reject duplicate round for one application")
    void databaseShouldRejectDuplicateApplicationRound() {

        LocalDateTime scheduledAt = LocalDateTime.of(
                2026,
                9,
                10,
                9,
                0);

        Interview first = InterviewTestFactory.interview(
                application,
                1,
                scheduledAt);

        interviewRepository.saveAndFlush(first);

        Interview duplicate = InterviewTestFactory.interview(
                application,
                1,
                scheduledAt.plusDays(1));

        assertThatThrownBy(() -> interviewRepository.saveAndFlush(
                duplicate))
                .isInstanceOf(
                        DataIntegrityViolationException.class);
    }
}
