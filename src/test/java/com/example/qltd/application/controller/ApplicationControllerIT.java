package com.example.qltd.application.controller;

import com.example.qltd.application.dto.request.ChangeApplicationStatusRequest;
import com.example.qltd.application.dto.request.CreateApplicationRequest;
import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Application Controller Integration Test")
class ApplicationControllerIT
        extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private User saveCandidate(
            String email) {

        return userRepository.save(
                ApplicationTestFactory
                        .candidate(email));
    }

    private User saveRecruiter(
            String email) {

        return userRepository.save(
                ApplicationTestFactory
                        .recruiter(email));
    }

    private Job savePublishedJob() {

        return jobRepository.save(
                ApplicationTestFactory
                        .publishedJob(company));
    }

    private Job saveDraftJob() {

        return jobRepository.save(
                ApplicationTestFactory
                        .draftJob(company));
    }

    private Application saveApplication(
            Job job,
            User candidate) {

        return applicationRepository.save(
                ApplicationTestFactory
                        .application(
                                job,
                                candidate));
    }

    @Nested
    @DisplayName("POST /api/jobs/{jobId}/applications")
    class CreateApplicationTest {

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should apply to published job")
        void candidateShouldApplyToPublishedJob()
                throws Exception {

            User candidate = saveCandidate(
                    "candidate@test.com");

            Job job = savePublishedJob();

            CreateApplicationRequest request = ApplicationTestFactory
                    .createRequest();

            mockMvc.perform(
                    post(
                            "/api/jobs/{jobId}/applications",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isCreated())
                    .andExpect(
                            jsonPath("$.success")
                                    .value(true))
                    .andExpect(
                            jsonPath("$.message")
                                    .value(
                                            "Application created successfully"))
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("APPLIED"))
                    .andExpect(
                            jsonPath("$.data.jobId")
                                    .value(job.getId()))
                    .andExpect(
                            jsonPath("$.data.candidateId")
                                    .value(candidate.getId()));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not apply twice")
        void candidateShouldNotApplyTwice()
                throws Exception {

            User candidate = saveCandidate(
                    "candidate@test.com");

            Job job = savePublishedJob();

            saveApplication(
                    job,
                    candidate);

            CreateApplicationRequest request = ApplicationTestFactory
                    .createRequest();

            mockMvc.perform(
                    post(
                            "/api/jobs/{jobId}/applications",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isConflict())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "DUPLICATE_RESOURCE"));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not apply to draft job")
        void candidateShouldNotApplyToDraftJob()
                throws Exception {

            saveCandidate(
                    "candidate@test.com");

            Job job = saveDraftJob();

            CreateApplicationRequest request = ApplicationTestFactory
                    .createRequest();

            mockMvc.perform(
                    post(
                            "/api/jobs/{jobId}/applications",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isBadRequest())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "BUSINESS_RULE_ERROR"));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should not apply")
        void recruiterShouldNotApply()
                throws Exception {

            saveRecruiter(
                    "recruiter@test.com");

            Job job = savePublishedJob();

            CreateApplicationRequest request = ApplicationTestFactory
                    .createRequest();

            mockMvc.perform(
                    post(
                            "/api/jobs/{jobId}/applications",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isForbidden());
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Should return 404 when job does not exist")
        void shouldReturn404WhenJobDoesNotExist()
                throws Exception {

            saveCandidate(
                    "candidate@test.com");

            CreateApplicationRequest request = ApplicationTestFactory
                    .createRequest();

            mockMvc.perform(
                    post(
                            "/api/jobs/{jobId}/applications",
                            999999L)
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isNotFound());
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Should reject invalid request")
        void shouldRejectInvalidRequest()
                throws Exception {

            saveCandidate(
                    "candidate@test.com");

            Job job = savePublishedJob();

            CreateApplicationRequest request = new CreateApplicationRequest();

            request.setCoverLetter(
                    "a".repeat(5001));

            mockMvc.perform(
                    post(
                            "/api/jobs/{jobId}/applications",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isBadRequest())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication()
                throws Exception {

            Job job = savePublishedJob();

            CreateApplicationRequest request = ApplicationTestFactory
                    .createRequest();

            mockMvc.perform(
                    post(
                            "/api/jobs/{jobId}/applications",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/applications/{id}")
    class GetApplicationTest {

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should get own application")
        void candidateShouldGetOwnApplication()
                throws Exception {

            User candidate = saveCandidate(
                    "candidate@test.com");

            Job job = savePublishedJob();

            Application application = saveApplication(
                    job,
                    candidate);

            mockMvc.perform(
                    get(
                            "/api/applications/{id}",
                            application.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.success")
                                    .value(true))
                    .andExpect(
                            jsonPath("$.data.id")
                                    .value(
                                            application.getId()))
                    .andExpect(
                            jsonPath("$.data.candidateId")
                                    .value(
                                            candidate.getId()))
                    .andExpect(
                            jsonPath("$.data.jobId")
                                    .value(
                                            job.getId()))
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("APPLIED"));
        }

        @Test
        @WithMockUser(username = "other@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not get another candidate application")
        void candidateShouldNotGetAnotherApplication()
                throws Exception {

            User owner = saveCandidate(
                    "owner@test.com");

            saveCandidate(
                    "other@test.com");

            Job job = savePublishedJob();

            Application application = saveApplication(
                    job,
                    owner);

            mockMvc.perform(
                    get(
                            "/api/applications/{id}",
                            application.getId()))
                    .andExpect(
                            status().isNotFound());
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should not access candidate own-application endpoint")
        void recruiterShouldNotGetMyApplication()
                throws Exception {

            User candidate = saveCandidate(
                    "candidate@test.com");

            Job job = savePublishedJob();

            Application application = saveApplication(
                    job,
                    candidate);

            mockMvc.perform(
                    get(
                            "/api/applications/{id}",
                            application.getId()))
                    .andExpect(
                            status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/applications/{id}/status")
    class ChangeStatusTest {

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should move application to SCREENING")
        void recruiterShouldMoveToScreening()
                throws Exception {

            User candidate = saveCandidate(
                    "candidate@test.com");

            saveRecruiter(
                    "recruiter@test.com");

            Job job = savePublishedJob();

            Application application = saveApplication(
                    job,
                    candidate);

            ChangeApplicationStatusRequest request = ApplicationTestFactory
                    .statusRequest(
                            ApplicationStatus.SCREENING);

            mockMvc.perform(
                    patch(
                            "/api/applications/{id}/status",
                            application.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.success")
                                    .value(true))
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("SCREENING"));

            Application updated = applicationRepository
                    .findById(
                            application.getId())
                    .orElseThrow();

            assertThat(
                    updated.getStatus())
                    .isEqualTo(
                            ApplicationStatus.SCREENING);
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should reject invalid APPLIED to HIRED transition")
        void shouldRejectInvalidTransition()
                throws Exception {

            User candidate = saveCandidate(
                    "candidate@test.com");

            Job job = savePublishedJob();

            Application application = saveApplication(
                    job,
                    candidate);

            ChangeApplicationStatusRequest request = ApplicationTestFactory
                    .statusRequest(
                            ApplicationStatus.HIRED);

            mockMvc.perform(
                    patch(
                            "/api/applications/{id}/status",
                            application.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isBadRequest())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "BUSINESS_RULE_ERROR"));

            Application unchanged = applicationRepository
                    .findById(
                            application.getId())
                    .orElseThrow();

            assertThat(
                    unchanged.getStatus())
                    .isEqualTo(
                            ApplicationStatus.APPLIED);
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not change application status")
        void candidateShouldNotChangeStatus()
                throws Exception {

            User candidate = saveCandidate(
                    "candidate@test.com");

            Job job = savePublishedJob();

            Application application = saveApplication(
                    job,
                    candidate);

            ChangeApplicationStatusRequest request = ApplicationTestFactory
                    .statusRequest(
                            ApplicationStatus.SCREENING);

            mockMvc.perform(
                    patch(
                            "/api/applications/{id}/status",
                            application.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("Admin should change application status")
        void adminShouldChangeStatus()
                throws Exception {

            User candidate = saveCandidate(
                    "candidate@test.com");

            Job job = savePublishedJob();

            Application application = saveApplication(
                    job,
                    candidate);

            ChangeApplicationStatusRequest request = ApplicationTestFactory
                    .statusRequest(
                            ApplicationStatus.SCREENING);

            mockMvc.perform(
                    patch(
                            "/api/applications/{id}/status",
                            application.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("SCREENING"));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should return 404 when application does not exist")
        void shouldReturn404ForMissingApplication()
                throws Exception {

            ChangeApplicationStatusRequest request = ApplicationTestFactory
                    .statusRequest(
                            ApplicationStatus.SCREENING);

            mockMvc.perform(
                    patch(
                            "/api/applications/{id}/status",
                            999999L)
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/applications/{id}/withdraw")
    class WithdrawApplicationTest {

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should withdraw own application")
        void candidateShouldWithdrawOwnApplication()
                throws Exception {

            User candidate = saveCandidate(
                    "candidate@test.com");

            Job job = savePublishedJob();

            Application application = saveApplication(
                    job,
                    candidate);

            mockMvc.perform(
                    post(
                            "/api/applications/{id}/withdraw",
                            application.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.success")
                                    .value(true))
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("WITHDRAWN"));

            Application updated = applicationRepository
                    .findById(
                            application.getId())
                    .orElseThrow();

            assertThat(
                    updated.getStatus())
                    .isEqualTo(
                            ApplicationStatus.WITHDRAWN);
        }

        @Test
        @WithMockUser(username = "other@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not withdraw another candidate application")
        void candidateShouldNotWithdrawAnotherApplication()
                throws Exception {

            User owner = saveCandidate(
                    "owner@test.com");

            saveCandidate(
                    "other@test.com");

            Job job = savePublishedJob();

            Application application = saveApplication(
                    job,
                    owner);

            mockMvc.perform(
                    post(
                            "/api/applications/{id}/withdraw",
                            application.getId()))
                    .andExpect(
                            status().isNotFound());

            Application unchanged = applicationRepository
                    .findById(
                            application.getId())
                    .orElseThrow();

            assertThat(
                    unchanged.getStatus())
                    .isEqualTo(
                            ApplicationStatus.APPLIED);
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should not withdraw application")
        void recruiterShouldNotWithdrawApplication()
                throws Exception {

            User candidate = saveCandidate(
                    "candidate@test.com");

            Job job = savePublishedJob();

            Application application = saveApplication(
                    job,
                    candidate);

            mockMvc.perform(
                    post(
                            "/api/applications/{id}/withdraw",
                            application.getId()))
                    .andExpect(
                            status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication()
                throws Exception {

            mockMvc.perform(
                    post(
                            "/api/applications/{id}/withdraw",
                            1L))
                    .andExpect(
                            status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/jobs/{jobId}/applications")
    class SearchApplicationsTest {

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should get paged applications")
        void recruiterShouldGetPagedApplications()
                throws Exception {

            User candidate1 = saveCandidate(
                    "candidate1@test.com");

            User candidate2 = saveCandidate(
                    "candidate2@test.com");

            Job job = savePublishedJob();

            saveApplication(
                    job,
                    candidate1);

            saveApplication(
                    job,
                    candidate2);

            mockMvc.perform(
                    get(
                            "/api/jobs/{jobId}/applications",
                            job.getId())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.success")
                                    .value(true))
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(2))
                    .andExpect(
                            jsonPath("$.data.items")
                                    .isArray());
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should filter applications by status")
        void shouldFilterApplicationsByStatus()
                throws Exception {

            User candidate1 = saveCandidate(
                    "candidate1@test.com");

            User candidate2 = saveCandidate(
                    "candidate2@test.com");

            Job job = savePublishedJob();

            Application screening = saveApplication(
                    job,
                    candidate1);

            screening.setStatus(
                    ApplicationStatus.SCREENING);

            applicationRepository.save(
                    screening);

            saveApplication(
                    job,
                    candidate2);

            mockMvc.perform(
                    get(
                            "/api/jobs/{jobId}/applications",
                            job.getId())
                            .param(
                                    "status",
                                    "SCREENING"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].status")
                                    .value("SCREENING"));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should paginate applications")
        void shouldPaginateApplications()
                throws Exception {

            Job job = savePublishedJob();

            for (int i = 1; i <= 15; i++) {

                User candidate = saveCandidate(
                        "pagination" +
                                i +
                                "@test.com");

                saveApplication(
                        job,
                        candidate);
            }

            mockMvc.perform(
                    get(
                            "/api/jobs/{jobId}/applications",
                            job.getId())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.items.length()")
                                    .value(10))
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(15))
                    .andExpect(
                            jsonPath("$.data.totalPages")
                                    .value(2));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should only return applications belonging to requested job")
        void shouldReturnApplicationsOnlyForRequestedJob()
                throws Exception {

            User candidate1 = saveCandidate(
                    "candidate1@test.com");

            User candidate2 = saveCandidate(
                    "candidate2@test.com");

            Job job1 = savePublishedJob();

            Job job2 = savePublishedJob();

            saveApplication(
                    job1,
                    candidate1);

            saveApplication(
                    job2,
                    candidate2);

            mockMvc.perform(
                    get(
                            "/api/jobs/{jobId}/applications",
                            job1.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].jobId")
                                    .value(job1.getId()));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not access job applications")
        void candidateShouldNotAccessApplications()
                throws Exception {

            Job job = savePublishedJob();

            mockMvc.perform(
                    get(
                            "/api/jobs/{jobId}/applications",
                            job.getId()))
                    .andExpect(
                            status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication()
                throws Exception {

            Job job = savePublishedJob();

            mockMvc.perform(
                    get(
                            "/api/jobs/{jobId}/applications",
                            job.getId()))
                    .andExpect(
                            status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should return 404 when job does not exist")
        void shouldReturn404WhenJobDoesNotExist()
                throws Exception {

            mockMvc.perform(
                    get(
                            "/api/jobs/{jobId}/applications",
                            999999L))
                    .andExpect(
                            status().isNotFound())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "RESOURCE_NOT_FOUND"));
        }
    }
}