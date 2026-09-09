package com.example.qltd.application.repository;

import com.example.qltd.application.dto.request.ApplicationSearchRequest;
import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.specification.ApplicationSpecification;
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
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Application Search Repository Integration Test")
class ApplicationSearchRepositoryIT
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
    @DisplayName("Should search applications by job")
    void shouldSearchApplicationsByJob() {

        Job job1 = jobRepository.save(
                ApplicationTestFactory
                        .publishedJob(company));

        Job job2 = jobRepository.save(
                ApplicationTestFactory
                        .publishedJob(company));

        User candidate1 = userRepository.save(
                ApplicationTestFactory
                        .candidate(
                                "candidate1@test.com"));

        User candidate2 = userRepository.save(
                ApplicationTestFactory
                        .candidate(
                                "candidate2@test.com"));

        Application application1 = applicationRepository.save(
                ApplicationTestFactory
                        .application(
                                job1,
                                candidate1));

        Application application2 = applicationRepository.save(
                ApplicationTestFactory
                        .application(
                                job2,
                                candidate2));

        ApplicationSearchRequest request = new ApplicationSearchRequest();

        var page = applicationRepository.findAll(
                ApplicationSpecification.search(
                        job1.getId(),
                        request),
                PageRequest.of(
                        0,
                        10));

        assertThat(
                page.getContent())
                .containsExactly(
                        application1);

        assertThat(
                page.getContent())
                .doesNotContain(
                        application2);
    }

    @Test
    @DisplayName("Should filter applications by status")
    void shouldFilterApplicationsByStatus() {

        Job job = jobRepository.save(
                ApplicationTestFactory
                        .publishedJob(company));

        User candidate1 = userRepository.save(
                ApplicationTestFactory
                        .candidate(
                                "status1@test.com"));

        User candidate2 = userRepository.save(
                ApplicationTestFactory
                        .candidate(
                                "status2@test.com"));

        Application screening = ApplicationTestFactory
                .application(
                        job,
                        candidate1,
                        ApplicationStatus.SCREENING);

        Application applied = ApplicationTestFactory
                .application(
                        job,
                        candidate2,
                        ApplicationStatus.APPLIED);

        applicationRepository.saveAll(
                List.of(
                        screening,
                        applied));

        ApplicationSearchRequest request = new ApplicationSearchRequest();

        request.setStatus(
                ApplicationStatus.SCREENING);

        var page = applicationRepository.findAll(
                ApplicationSpecification.search(
                        job.getId(),
                        request),
                PageRequest.of(
                        0,
                        10));

        assertThat(
                page.getTotalElements())
                .isEqualTo(1);

        assertThat(
                page.getContent()
                        .get(0)
                        .getStatus())
                .isEqualTo(
                        ApplicationStatus.SCREENING);
    }
}