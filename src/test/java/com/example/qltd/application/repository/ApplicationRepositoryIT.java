package com.example.qltd.application.repository;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Application Repository Integration Test")
class ApplicationRepositoryIT
        extends AbstractIntegrationTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    private Company company;

    @BeforeEach
    void setUp() {

        applicationRepository.deleteAll();

        jobRepository.deleteAll();

        companyRepository.deleteAll();

        userRepository.deleteAll();

        company = companyRepository.save(
                ApplicationTestFactory.company());
    }

    @Test
    @DisplayName("Database should reject duplicate job candidate application")
    void databaseShouldRejectDuplicateApplication() {

        User candidate = userRepository.save(
                ApplicationTestFactory
                        .candidate(
                                "candidate@test.com"));

        Job job = jobRepository.save(
                ApplicationTestFactory
                        .publishedJob(company));

        Application first = ApplicationTestFactory
                .application(
                        job,
                        candidate);

        applicationRepository.saveAndFlush(
                first);

        Application duplicate = ApplicationTestFactory
                .application(
                        job,
                        candidate);

        assertThatThrownBy(() -> applicationRepository.saveAndFlush(
                duplicate))
                .isInstanceOf(
                        DataIntegrityViolationException.class);
    }
}