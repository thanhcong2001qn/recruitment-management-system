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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Application candidate search repository integration test")
class ApplicationCandidateSearchRepositoryIT
        extends AbstractIntegrationTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company company;

    private Job job;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();

        company = companyRepository.save(
                ApplicationTestFactory.company());
        job = jobRepository.save(
                ApplicationTestFactory.publishedJob(company));
    }

    @Test
    @DisplayName("Should search candidate name case-insensitively")
    void shouldSearchCandidateNameCaseInsensitively() {
        saveApplication(
                "Nguyen Van An",
                "an@example.com",
                job,
                ApplicationStatus.APPLIED);
        saveApplication(
                "Tran Thi Binh",
                "binh@example.com",
                job,
                ApplicationStatus.APPLIED);

        ApplicationSearchRequest request = new ApplicationSearchRequest();
        request.setKeyword("vAn aN");

        Page<Application> result = applicationRepository.findAll(
                ApplicationSpecification.search(job.getId(), request),
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0)
                .getCandidate().getFullName())
                .isEqualTo("Nguyen Van An");
    }

    @Test
    @DisplayName("Should search candidate email case-insensitively")
    void shouldSearchCandidateEmailCaseInsensitively() {
        saveApplication(
                "Nguyen Van An",
                "an@example.com",
                job,
                ApplicationStatus.APPLIED);
        saveApplication(
                "Tran Thi Binh",
                "binh@example.com",
                job,
                ApplicationStatus.APPLIED);

        ApplicationSearchRequest request = new ApplicationSearchRequest();
        request.setKeyword("BINH@EXAMPLE");

        Page<Application> result = applicationRepository.findAll(
                ApplicationSpecification.search(job.getId(), request),
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0)
                .getCandidate().getEmail())
                .isEqualTo("binh@example.com");
    }

    @Test
    @DisplayName("Should combine candidate keyword and status")
    void shouldCombineCandidateKeywordAndStatus() {
        saveApplication(
                "Nguyen Van An",
                "an@example.com",
                job,
                ApplicationStatus.APPLIED);
        saveApplication(
                "Tran Thi Binh",
                "binh@example.com",
                job,
                ApplicationStatus.SCREENING);

        ApplicationSearchRequest request = new ApplicationSearchRequest();
        request.setKeyword("example.com");
        request.setStatus(ApplicationStatus.SCREENING);

        Page<Application> result = applicationRepository.findAll(
                ApplicationSpecification.search(job.getId(), request),
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getStatus())
                .isEqualTo(ApplicationStatus.SCREENING);
        assertThat(result.getContent().get(0)
                .getCandidate().getEmail())
                .isEqualTo("binh@example.com");
    }

    @Test
    @DisplayName("Should keep search isolated to requested job")
    void shouldKeepSearchIsolatedToRequestedJob() {
        Job anotherJob = jobRepository.save(
                ApplicationTestFactory.publishedJob(company));

        saveApplication(
                "Matching Candidate One",
                "one@example.com",
                job,
                ApplicationStatus.APPLIED);
        saveApplication(
                "Matching Candidate Two",
                "two@example.com",
                anotherJob,
                ApplicationStatus.APPLIED);

        ApplicationSearchRequest request = new ApplicationSearchRequest();
        request.setKeyword("Matching Candidate");

        Page<Application> result = applicationRepository.findAll(
                ApplicationSpecification.search(job.getId(), request),
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getJob().getId())
                .isEqualTo(job.getId());
    }

    private Application saveApplication(
            String fullName,
            String email,
            Job applicationJob,
            ApplicationStatus status) {
        User candidate = ApplicationTestFactory.candidate(email);
        candidate.setFullName(fullName);
        candidate = userRepository.save(candidate);

        return applicationRepository.save(
                ApplicationTestFactory.application(
                        applicationJob,
                        candidate,
                        status));
    }
}
