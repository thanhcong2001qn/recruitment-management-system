package com.example.qltd.job.controller;

import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.EmploymentType;
import com.example.qltd.job.enums.ExperienceLevel;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.enums.WorkingType;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.job.support.JobTestFactory;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.shared.enums.UserStatus;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@DisplayName("Job Controller Integration Test")
class JobControllerIT
        extends AbstractIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    private Company openAi;

    private Company microsoft;

    private Job saveJobWithStatus(
            String title,
            String slug,
            Company company,
            JobStatus status) {

        Job job = JobTestFactory.job(company);

        job.setId(null);
        job.setTitle(title);
        job.setSlug(slug);
        job.setStatus(status);

        return jobRepository.save(job);
    }

    @BeforeEach
    void setUp() {

        jobRepository.deleteAll();

        userRepository.deleteAll();

        companyRepository.deleteAll();

        openAi = companyRepository.save(
                JobTestFactory.activeCompany());

        microsoft = companyRepository.save(
                JobTestFactory
                        .secondActiveCompany());

        userRepository.save(
                User.builder()
                        .fullName("Test Recruiter")
                        .email("recruiter@test.com")
                        .password("password")
                        .role(Role.RECRUITER)
                        .status(UserStatus.ACTIVE)
                        .company(openAi)
                        .build());

        userRepository.save(
                User.builder()
                        .fullName("Test Admin")
                        .email("admin@test.com")
                        .password("password")
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build());
    }

    private Job saveJob(
            String title,
            String slug,
            Company company) {

        Job job = JobTestFactory.job(
                company);

        job.setId(null);

        job.setTitle(title);

        job.setSlug(slug);

        return jobRepository.save(job);
    }

    @Nested
    @DisplayName("POST /api/jobs")
    class CreateJobTest {

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should create job for own company")
        void recruiterShouldCreateJobForOwnCompany()
                throws Exception {

            CreateJobRequest request = JobTestFactory.createRequest();
            request.setCompanyId(openAi.getId());

            mockMvc.perform(
                    post("/api/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.companyId")
                            .value(openAi.getId()));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should not create job for another company")
        void recruiterShouldNotCreateJobForAnotherCompany()
                throws Exception {

            CreateJobRequest request = JobTestFactory.createRequest();
            request.setCompanyId(microsoft.getId());

            long jobCount = jobRepository.count();

            mockMvc.perform(
                    post("/api/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("FORBIDDEN"));

            assertThat(jobRepository.count()).isEqualTo(jobCount);
        }

        @Test
        @WithMockUser(username = "unassigned@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter without company should not create job")
        void recruiterWithoutCompanyShouldNotCreateJob()
                throws Exception {

            userRepository.save(
                    User.builder()
                            .fullName("Unassigned Recruiter")
                            .email("unassigned@test.com")
                            .password("password")
                            .role(Role.RECRUITER)
                            .status(UserStatus.ACTIVE)
                            .build());

            CreateJobRequest request = JobTestFactory.createRequest();
            request.setCompanyId(openAi.getId());

            mockMvc.perform(
                    post("/api/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("Admin should create job for any company")
        void adminShouldCreateJobForAnyCompany()
                throws Exception {

            CreateJobRequest request = JobTestFactory.createRequest();
            request.setCompanyId(microsoft.getId());

            mockMvc.perform(
                    post("/api/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.companyId")
                            .value(microsoft.getId()));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not create job")
        void candidateShouldNotCreateJob()
                throws Exception {

            CreateJobRequest request = JobTestFactory.createRequest();
            request.setCompanyId(openAi.getId());

            mockMvc.perform(
                    post("/api/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @WithMockUser(username = "test-user", roles = "ADMIN")
    @DisplayName("GET /api/jobs")
    class SearchJobs {

        @Test
        @DisplayName("Should return paged jobs")
        void shouldReturnPagedJobs()
                throws Exception {

            saveJob(
                    "Java Backend Developer",
                    "java-backend-developer",
                    openAi);

            saveJob(
                    "Frontend Developer",
                    "frontend-developer",
                    microsoft);

            mockMvc.perform(
                    get("/api/jobs")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.success").value(true))
                    .andExpect(
                            jsonPath(
                                    "$.data.items").isArray())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("Should search by keyword in title")
        void shouldSearchByKeywordInTitle()
                throws Exception {

            saveJob(
                    "Java Backend Developer",
                    "java-backend-developer",
                    openAi);

            saveJob(
                    "Frontend Developer",
                    "frontend-developer",
                    microsoft);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "keyword",
                                    "Java"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("Should search by keyword in description")
        void shouldSearchByKeywordInDescription()
                throws Exception {

            Job javaJob = JobTestFactory.job(
                    openAi);

            javaJob.setId(null);
            javaJob.setTitle(
                    "Backend Developer");
            javaJob.setSlug(
                    "backend-developer");
            javaJob.setDescription(
                    "Build Java microservices.");

            jobRepository.save(javaJob);

            Job frontendJob = JobTestFactory.job(
                    microsoft);

            frontendJob.setId(null);
            frontendJob.setTitle(
                    "Frontend Developer");
            frontendJob.setSlug(
                    "frontend-developer");
            frontendJob.setDescription(
                    "Build React applications.");

            jobRepository.save(frontendJob);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "keyword",
                                    "microservices"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].title")
                                    .value(
                                            "Backend Developer"));
        }

        @Test
        @DisplayName("Should search by company")
        void shouldSearchByCompany()
                throws Exception {

            saveJob(
                    "Java Developer",
                    "java-developer",
                    openAi);

            saveJob(
                    "Frontend Developer",
                    "frontend-developer",
                    microsoft);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "companyId",
                                    openAi.getId()
                                            .toString()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].companyId")
                                    .value(
                                            openAi.getId()));
        }

        @Test
        @DisplayName("Should search by location")
        void shouldSearchByLocation()
                throws Exception {

            Job hoChiMinhJob = saveJob(
                    "Java Developer",
                    "java-developer-hcm",
                    openAi);

            hoChiMinhJob.setLocation(
                    "Ho Chi Minh");

            jobRepository.save(
                    hoChiMinhJob);

            Job haNoiJob = saveJob(
                    "Java Developer",
                    "java-developer-hanoi",
                    microsoft);

            haNoiJob.setLocation(
                    "Ha Noi");

            jobRepository.save(
                    haNoiJob);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "location",
                                    "Ho Chi Minh"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("Should search by status")
        void shouldSearchByStatus()
                throws Exception {

            Job published = saveJob(
                    "Published Job",
                    "published-job",
                    openAi);

            published.setStatus(
                    JobStatus.PUBLISHED);

            jobRepository.save(
                    published);

            Job draft = JobTestFactory.draftJob(
                    microsoft);

            draft.setId(null);
            draft.setTitle("Draft Job");
            draft.setSlug("draft-job");

            jobRepository.save(draft);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "status",
                                    "PUBLISHED"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].status")
                                    .value(
                                            "PUBLISHED"));
        }

        @Test
        @DisplayName("Should search by working type")
        void shouldSearchByWorkingType()
                throws Exception {

            Job hybrid = saveJob(
                    "Hybrid Developer",
                    "hybrid-developer",
                    openAi);

            hybrid.setWorkingType(
                    WorkingType.HYBRID);

            jobRepository.save(hybrid);

            Job remote = saveJob(
                    "Remote Developer",
                    "remote-developer",
                    microsoft);

            remote.setWorkingType(
                    WorkingType.REMOTE);

            jobRepository.save(remote);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "workingType",
                                    "REMOTE"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].workingType")
                                    .value(
                                            "REMOTE"));
        }

        @Test
        @DisplayName("Should search by experience level")
        void shouldSearchByExperienceLevel()
                throws Exception {

            Job junior = saveJob(
                    "Junior Developer",
                    "junior-developer",
                    openAi);

            junior.setExperienceLevel(
                    ExperienceLevel.JUNIOR);

            jobRepository.save(junior);

            Job senior = saveJob(
                    "Senior Developer",
                    "senior-developer",
                    microsoft);

            senior.setExperienceLevel(
                    ExperienceLevel.SENIOR);

            jobRepository.save(senior);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "experienceLevel",
                                    "SENIOR"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].experienceLevel")
                                    .value(
                                            "SENIOR"));
        }

        @Test
        @DisplayName("Should search by employment type")
        void shouldSearchByEmploymentType()
                throws Exception {

            Job fullTime = saveJob(
                    "Full Time Developer",
                    "full-time-developer",
                    openAi);

            fullTime.setEmploymentType(
                    EmploymentType.FULL_TIME);

            jobRepository.save(fullTime);

            Job contract = saveJob(
                    "Contract Developer",
                    "contract-developer",
                    microsoft);

            contract.setEmploymentType(
                    EmploymentType.CONTRACT);

            jobRepository.save(contract);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "employmentType",
                                    "CONTRACT"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].employmentType")
                                    .value(
                                            "CONTRACT"));
        }

        @Test
        @DisplayName("Should combine multiple filters")
        void shouldCombineMultipleFilters()
                throws Exception {

            Job matchingJob = saveJob(
                    "Java Backend Developer",
                    "java-backend-developer",
                    openAi);

            matchingJob.setLocation(
                    "Ho Chi Minh");

            matchingJob.setStatus(
                    JobStatus.PUBLISHED);

            matchingJob.setWorkingType(
                    WorkingType.HYBRID);

            matchingJob.setExperienceLevel(
                    ExperienceLevel.JUNIOR);

            matchingJob.setEmploymentType(
                    EmploymentType.FULL_TIME);

            jobRepository.save(
                    matchingJob);

            Job nonMatchingJob = saveJob(
                    "Java Backend Developer",
                    "java-backend-developer-2",
                    microsoft);

            nonMatchingJob.setLocation(
                    "Ha Noi");

            nonMatchingJob.setStatus(
                    JobStatus.PUBLISHED);

            jobRepository.save(
                    nonMatchingJob);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "keyword",
                                    "Java")
                            .param(
                                    "companyId",
                                    openAi.getId()
                                            .toString())
                            .param(
                                    "location",
                                    "Ho Chi Minh")
                            .param(
                                    "status",
                                    "PUBLISHED")
                            .param(
                                    "workingType",
                                    "HYBRID")
                            .param(
                                    "experienceLevel",
                                    "JUNIOR")
                            .param(
                                    "employmentType",
                                    "FULL_TIME"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].title")
                                    .value(
                                            "Java Backend Developer"));
        }

        @Test
        @DisplayName("Should exclude soft deleted jobs")
        void shouldExcludeSoftDeletedJobs()
                throws Exception {

            Job activeJob = saveJob(
                    "Active Job",
                    "active-job",
                    openAi);

            Job deletedJob = JobTestFactory.deletedJob(
                    openAi);

            deletedJob.setId(null);
            deletedJob.setTitle(
                    "Deleted Job");
            deletedJob.setSlug(
                    "deleted-job");

            jobRepository.save(
                    deletedJob);

            mockMvc.perform(
                    get("/api/jobs"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].title")
                                    .value(
                                            "Active Job"));

            assertThat(
                    jobRepository.findById(
                            deletedJob.getId()))
                    .isPresent();
        }

        @Test
        @DisplayName("Should return empty result")
        void shouldReturnEmptyResult()
                throws Exception {

            saveJob(
                    "Java Developer",
                    "java-developer",
                    openAi);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "keyword",
                                    "Python"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(0))
                    .andExpect(
                            jsonPath(
                                    "$.data.items").isEmpty());
        }

        @Test
        @DisplayName("Should paginate jobs")
        void shouldPaginateJobs()
                throws Exception {

            for (int i = 1; i <= 15; i++) {

                saveJob(
                        "Java Developer " + i,
                        "java-developer-" + i,
                        openAi);
            }

            mockMvc.perform(
                    get("/api/jobs")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.items.length()").value(5))
                    .andExpect(
                            jsonPath(
                                    "$.data.totalElements").value(15))
                    .andExpect(
                            jsonPath(
                                    "$.data.totalPages").value(3));
        }

        @Test
        @DisplayName("Should sort jobs by title ascending")
        void shouldSortJobsByTitleAscending()
                throws Exception {

            saveJob(
                    "Java Developer",
                    "java-developer",
                    openAi);

            saveJob(
                    "Backend Developer",
                    "backend-developer",
                    microsoft);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "sort",
                                    "title,asc"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].title")
                                    .value(
                                            "Backend Developer"));
        }

        @Test
        @DisplayName("Should sort jobs by title descending")
        void shouldSortJobsByTitleDescending()
                throws Exception {

            saveJob(
                    "Java Developer",
                    "java-developer",
                    openAi);

            saveJob(
                    "Backend Developer",
                    "backend-developer",
                    microsoft);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "sort",
                                    "title,desc"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].title")
                                    .value(
                                            "Java Developer"));
        }
    }

    @Nested
    @DisplayName("Public job visibility")
    class PublicJobVisibilityTest {
        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should see only PUBLISHED jobs")
        void candidateShouldSeeOnlyPublishedJobs()
                throws Exception {

            saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            saveJobWithStatus(
                    "Expired Job",
                    "expired-job",
                    openAi,
                    JobStatus.EXPIRED);

            saveJobWithStatus(
                    "Archived Job",
                    "archived-job",
                    openAi,
                    JobStatus.ARCHIVED);

            mockMvc.perform(
                    get("/api/jobs"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.success")
                                    .value(true))
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(1))
                    .andExpect(
                            jsonPath("$.data.items[0].title")
                                    .value("Published Job"))
                    .andExpect(
                            jsonPath("$.data.items[0].status")
                                    .value("PUBLISHED"));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not see DRAFT jobs")
        void candidateShouldNotSeeDraftJobs()
                throws Exception {

            saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "status",
                                    "DRAFT"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(0));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not see CLOSED jobs")
        void candidateShouldNotSeeClosedJobs()
                throws Exception {

            saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "status",
                                    "CLOSED"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(0));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not see ARCHIVED jobs")
        void candidateShouldNotSeeArchivedJobs()
                throws Exception {

            saveJobWithStatus(
                    "Archived Job",
                    "archived-job",
                    openAi,
                    JobStatus.ARCHIVED);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "status",
                                    "ARCHIVED"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(0));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should see CLOSED jobs")
        void recruiterShouldSeeClosedJobs()
                throws Exception {

            saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            mockMvc.perform(
                    get("/api/jobs/manage")
                            .param(
                                    "status",
                                    "CLOSED"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(1));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should see all management statuses")
        void recruiterShouldSeeAllManagementStatuses()
                throws Exception {

            saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            mockMvc.perform(
                    get("/api/jobs/manage"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(3));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should only see published jobs even with other filters")
        void candidateShouldOnlySeePublishedJobsWithOtherFilters()
                throws Exception {

            Job published = saveJobWithStatus(
                    "Java Developer",
                    "java-developer",
                    openAi,
                    JobStatus.PUBLISHED);

            published.setLocation("Ho Chi Minh");

            jobRepository.save(published);

            Job draft = saveJobWithStatus(
                    "Java Developer Draft",
                    "java-developer-draft",
                    openAi,
                    JobStatus.DRAFT);

            draft.setLocation("Ho Chi Minh");

            jobRepository.save(draft);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "keyword",
                                    "Java")
                            .param(
                                    "location",
                                    "Ho Chi Minh"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(1))
                    .andExpect(
                            jsonPath("$.data.items[0].status")
                                    .value("PUBLISHED"));
        }

        @Test
        @DisplayName("Should return 401 when requesting jobs without authentication")
        void shouldReturn401WhenUnauthenticated()
                throws Exception {

            mockMvc.perform(
                    get("/api/jobs"))
                    .andExpect(
                            status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/jobs/{id}")
    class GetJobByIdTest {

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should get published job")
        void candidateShouldGetPublishedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            mockMvc.perform(
                    get(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.success")
                                    .value(true))
                    .andExpect(
                            jsonPath("$.message")
                                    .value(
                                            "Job retrieved successfully"))
                    .andExpect(
                            jsonPath("$.data.id")
                                    .value(job.getId()))
                    .andExpect(
                            jsonPath("$.data.title")
                                    .value(
                                            "Published Job"))
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("PUBLISHED"))
                    .andExpect(
                            jsonPath("$.data.companyId")
                                    .value(openAi.getId()));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not get DRAFT job")
        void candidateShouldNotGetDraftJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    get(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isNotFound())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "RESOURCE_NOT_FOUND"));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not get CLOSED job")
        void candidateShouldNotGetClosedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            mockMvc.perform(
                    get(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isNotFound());
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not get EXPIRED job")
        void candidateShouldNotGetExpiredJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Expired Job",
                    "expired-job",
                    openAi,
                    JobStatus.EXPIRED);

            mockMvc.perform(
                    get(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isNotFound());
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not get ARCHIVED job")
        void candidateShouldNotGetArchivedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Archived Job",
                    "archived-job",
                    openAi,
                    JobStatus.ARCHIVED);

            mockMvc.perform(
                    get(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isNotFound());
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should get DRAFT job")
        void recruiterShouldGetDraftJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    get(
                            "/api/jobs/manage/{id}",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("DRAFT"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("Admin should get any active job")
        void adminShouldGetAnyActiveJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            mockMvc.perform(
                    get(
                            "/api/jobs/manage/{id}",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("CLOSED"));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should return 404 for non-existing job")
        void shouldReturn404ForNonExistingJob()
                throws Exception {

            mockMvc.perform(
                    get(
                            "/api/jobs/{id}",
                            999999L))
                    .andExpect(
                            status().isNotFound())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "RESOURCE_NOT_FOUND"));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not get soft deleted job")
        void candidateShouldNotGetDeletedJob()
                throws Exception {

            Job job = JobTestFactory.deletedJob(
                    openAi);

            job.setId(null);
            job.setTitle("Deleted Job");
            job.setSlug("deleted-job");

            job = jobRepository.save(job);

            mockMvc.perform(
                    get(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication()
                throws Exception {

            mockMvc.perform(
                    get(
                            "/api/jobs/{id}",
                            1L))
                    .andExpect(
                            status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/jobs/{id}")
    class UpdateJobTest {

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should update DRAFT job")
        void recruiterShouldUpdateDraftJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Java Developer",
                    "java-developer",
                    openAi,
                    JobStatus.DRAFT);

            UpdateJobRequest request = JobTestFactory.updateRequest();

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
                            job.getId())
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
                            jsonPath("$.data.title")
                                    .value(
                                            "Senior Java Backend Developer"));

            Job updated = jobRepository.findById(
                    job.getId()).orElseThrow();

            assertThat(updated.getTitle())
                    .isEqualTo(
                            "Senior Java Backend Developer");

            assertThat(updated.getSlug())
                    .isEqualTo(
                            "senior-java-backend-developer");
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should update PUBLISHED job")
        void recruiterShouldUpdatePublishedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Java Developer",
                    "java-developer",
                    openAi,
                    JobStatus.PUBLISHED);

            UpdateJobRequest request = new UpdateJobRequest();

            request.setDescription(
                    "Updated published job description.");

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isOk());

            Job updated = jobRepository.findById(
                    job.getId()).orElseThrow();

            assertThat(updated.getDescription())
                    .isEqualTo(
                            "Updated published job description.");
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should return 404 for non-existing job")
        void shouldReturn404ForNonExistingJobOnUpdate()
                throws Exception {

            UpdateJobRequest request = JobTestFactory.updateRequest();

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
                            999999L)
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isNotFound())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "RESOURCE_NOT_FOUND"));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should reject update for CLOSED job")
        void shouldRejectUpdateForClosedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            UpdateJobRequest request = JobTestFactory.updateRequest();

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
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
        @DisplayName("Should reject update for EXPIRED job")
        void shouldRejectUpdateForExpiredJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Expired Job",
                    "expired-job",
                    openAi,
                    JobStatus.EXPIRED);

            UpdateJobRequest request = JobTestFactory.updateRequest();

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should reject update for ARCHIVED job")
        void shouldRejectUpdateForArchivedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Archived Job",
                    "archived-job",
                    openAi,
                    JobStatus.ARCHIVED);

            UpdateJobRequest request = JobTestFactory.updateRequest();

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should keep slug when title does not change")
        void shouldKeepSlugWhenTitleDoesNotChange()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Java Developer",
                    "java-developer",
                    openAi,
                    JobStatus.DRAFT);

            UpdateJobRequest request = new UpdateJobRequest();

            request.setTitle(
                    "Java Developer");

            request.setDescription(
                    "Updated description.");

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isOk());

            Job updated = jobRepository.findById(
                    job.getId()).orElseThrow();

            assertThat(updated.getSlug())
                    .isEqualTo(
                            "java-developer");
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should validate salary range when updating")
        void shouldValidateSalaryRange()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Java Developer",
                    "java-developer",
                    openAi,
                    JobStatus.DRAFT);

            UpdateJobRequest request = new UpdateJobRequest();

            request.setSalaryMin(
                    new BigDecimal("40000000"));

            request.setSalaryMax(
                    new BigDecimal("20000000"));

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
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
        @DisplayName("Should reject past deadline")
        void shouldRejectPastDeadline()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Java Developer",
                    "java-developer",
                    openAi,
                    JobStatus.DRAFT);

            UpdateJobRequest request = new UpdateJobRequest();

            request.setDeadline(
                    LocalDate.now().minusDays(1));

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
                            job.getId())
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(
                                            request)))
                    .andExpect(
                            status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should not update another company's job")
        void recruiterShouldNotUpdateAnotherCompanyJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Microsoft Job",
                    "microsoft-job",
                    microsoft,
                    JobStatus.DRAFT);

            UpdateJobRequest request = new UpdateJobRequest();
            request.setTitle("Unauthorized Update");

            mockMvc.perform(
                    put("/api/jobs/{id}", job.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("FORBIDDEN"));

            Job unchanged = jobRepository.findById(job.getId()).orElseThrow();
            assertThat(unchanged.getTitle()).isEqualTo("Microsoft Job");
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not update job")
        void candidateShouldNotUpdateJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Java Developer",
                    "java-developer",
                    openAi,
                    JobStatus.DRAFT);

            UpdateJobRequest request = JobTestFactory.updateRequest();

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
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
        @DisplayName("Should return 401 when updating without authentication")
        void shouldReturn401WithoutAuthentication()
                throws Exception {

            UpdateJobRequest request = JobTestFactory.updateRequest();

            mockMvc.perform(
                    put(
                            "/api/jobs/{id}",
                            1L)
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
    @DisplayName("DELETE /api/jobs/{id}")
    class DeleteJobTest {

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should soft delete DRAFT job")
        void recruiterShouldSoftDeleteDraftJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    delete(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.success")
                                    .value(true))
                    .andExpect(
                            jsonPath("$.message")
                                    .value(
                                            "Job deleted successfully"));

            Job deleted = jobRepository.findById(
                    job.getId()).orElseThrow();

            assertThat(deleted.getDeleted())
                    .isTrue();
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("Admin should soft delete CLOSED job")
        void adminShouldSoftDeleteClosedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            mockMvc.perform(
                    delete(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isOk());

            Job deleted = jobRepository.findById(
                    job.getId()).orElseThrow();

            assertThat(deleted.getDeleted())
                    .isTrue();

            assertThat(deleted.getStatus())
                    .isEqualTo(
                            JobStatus.CLOSED);
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should reject deleting PUBLISHED job")
        void shouldRejectDeletingPublishedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            mockMvc.perform(
                    delete(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isBadRequest())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "BUSINESS_RULE_ERROR"));

            Job unchanged = jobRepository.findById(
                    job.getId()).orElseThrow();

            assertThat(unchanged.getDeleted())
                    .isFalse();
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("Should reject deleting ARCHIVED job")
        void shouldRejectDeletingArchivedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Archived Job",
                    "archived-job",
                    openAi,
                    JobStatus.ARCHIVED);

            mockMvc.perform(
                    delete(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isBadRequest());

            Job unchanged = jobRepository.findById(
                    job.getId()).orElseThrow();

            assertThat(unchanged.getDeleted())
                    .isFalse();
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should return 404 when deleting non-existing job")
        void shouldReturn404WhenDeletingNonExistingJob()
                throws Exception {

            mockMvc.perform(
                    delete(
                            "/api/jobs/{id}",
                            999999L))
                    .andExpect(
                            status().isNotFound())
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "RESOURCE_NOT_FOUND"));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should not delete another company's job")
        void recruiterShouldNotDeleteAnotherCompanyJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Microsoft Draft",
                    "microsoft-draft",
                    microsoft,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    delete("/api/jobs/{id}", job.getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("FORBIDDEN"));

            Job unchanged = jobRepository.findById(job.getId()).orElseThrow();
            assertThat(unchanged.getDeleted()).isFalse();
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not delete job")
        void candidateShouldNotDeleteJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Draft Job",
                    "candidate-draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    delete(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isForbidden());

            Job unchanged = jobRepository.findById(
                    job.getId()).orElseThrow();

            assertThat(unchanged.getDeleted())
                    .isFalse();
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication()
                throws Exception {

            mockMvc.perform(
                    delete(
                            "/api/jobs/{id}",
                            1L))
                    .andExpect(
                            status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should return 404 when deleting already deleted job")
        void shouldReturn404WhenDeletingAlreadyDeletedJob()
                throws Exception {

            Job job = JobTestFactory.deletedJob(
                    openAi);

            job.setId(null);
            job.setTitle("Already Deleted");
            job.setSlug("already-deleted");

            job = jobRepository.save(job);

            mockMvc.perform(
                    delete(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/jobs/{id}/publish")
    class PublishJobTest {

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should publish DRAFT job")
        void recruiterShouldPublishDraftJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Java Developer",
                    "java-developer",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    post(
                            "/api/jobs/{id}/publish",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.success")
                                    .value(true))
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("PUBLISHED"));

            Job updated = jobRepository.findById(
                    job.getId()).orElseThrow();

            assertThat(updated.getStatus())
                    .isEqualTo(
                            JobStatus.PUBLISHED);
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should reject publishing CLOSED job")
        void shouldRejectPublishingClosedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            mockMvc.perform(
                    post(
                            "/api/jobs/{id}/publish",
                            job.getId()))
                    .andExpect(
                            status().isBadRequest());

            Job unchanged = jobRepository.findById(
                    job.getId()).orElseThrow();

            assertThat(unchanged.getStatus())
                    .isEqualTo(
                            JobStatus.CLOSED);
        }
    }

    @Nested
    @DisplayName("POST /api/jobs/{id}/close")
    class CloseJobTest {

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should close PUBLISHED job")
        void recruiterShouldClosePublishedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            mockMvc.perform(
                    post(
                            "/api/jobs/{id}/close",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("CLOSED"));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should reject closing DRAFT job")
        void shouldRejectClosingDraftJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    post(
                            "/api/jobs/{id}/close",
                            job.getId()))
                    .andExpect(
                            status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/jobs/{id}/expire")
    class ExpireJobTest {

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should expire PUBLISHED job")
        void recruiterShouldExpirePublishedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            mockMvc.perform(
                    post(
                            "/api/jobs/{id}/expire",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("EXPIRED"));
        }
    }

    @Nested
    @DisplayName("POST /api/jobs/{id}/archive")
    class ArchiveJobTest {

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should archive CLOSED job")
        void recruiterShouldArchiveClosedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            mockMvc.perform(
                    post(
                            "/api/jobs/{id}/archive",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("ARCHIVED"));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should archive EXPIRED job")
        void recruiterShouldArchiveExpiredJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Expired Job",
                    "expired-job",
                    openAi,
                    JobStatus.EXPIRED);

            mockMvc.perform(
                    post(
                            "/api/jobs/{id}/archive",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("ARCHIVED"));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Should reject archiving PUBLISHED job")
        void shouldRejectArchivingPublishedJob()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            mockMvc.perform(
                    post(
                            "/api/jobs/{id}/archive",
                            job.getId()))
                    .andExpect(
                            status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Public job API")
    class PublicJobApiTest {

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should only see published jobs")
        void candidateShouldOnlySeePublishedJobs()
                throws Exception {

            saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            saveJobWithStatus(
                    "Closed Job",
                    "closed-job",
                    openAi,
                    JobStatus.CLOSED);

            mockMvc.perform(
                    get("/api/jobs"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].status")
                                    .value("PUBLISHED"));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate cannot bypass visibility with status filter")
        void candidateCannotBypassVisibility()
                throws Exception {

            saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    get("/api/jobs")
                            .param(
                                    "status",
                                    "DRAFT"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(0));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate can get published job details")
        void candidateCanGetPublishedJobDetails()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            mockMvc.perform(
                    get(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.data.status")
                                    .value("PUBLISHED"));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate cannot get draft job details")
        void candidateCannotGetDraftJobDetails()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    get(
                            "/api/jobs/{id}",
                            job.getId()))
                    .andExpect(
                            status().isNotFound());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "publish", "close", "expire", "archive" })
    @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should not change status of another company's job")
    void recruiterShouldNotChangeStatusOfAnotherCompanyJob(
            String action) throws Exception {

        Job job = saveJobWithStatus(
                "Microsoft Job",
                "microsoft-status-job-" + action,
                microsoft,
                JobStatus.DRAFT);

        mockMvc.perform(
                post("/api/jobs/{id}/{action}", job.getId(), action))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        Job unchanged = jobRepository.findById(job.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(JobStatus.DRAFT);
    }

    @Nested
    @DisplayName("Management job API")
    class ManagementJobApiTest {

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should see draft jobs")
        void recruiterShouldSeeDraftJobs()
                throws Exception {

            saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    get("/api/jobs/manage"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(1))
                    .andExpect(
                            jsonPath(
                                    "$.data.items[0].status")
                                    .value("DRAFT"));
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should filter draft jobs")
        void recruiterShouldFilterDraftJobs()
                throws Exception {

            saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            saveJobWithStatus(
                    "Published Job",
                    "published-job",
                    openAi,
                    JobStatus.PUBLISHED);

            mockMvc.perform(
                    get("/api/jobs/manage")
                            .param(
                                    "status",
                                    "DRAFT"))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(1));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not access management search")
        void candidateShouldNotAccessManagementSearch()
                throws Exception {

            mockMvc.perform(
                    get("/api/jobs/manage"))
                    .andExpect(
                            status().isForbidden());
        }

        @Test
        @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
        @DisplayName("Recruiter should get draft job from management API")
        void recruiterShouldGetDraftJobFromManagementApi()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    get(
                            "/api/jobs/manage/{id}",
                            job.getId()))
                    .andExpect(
                            status().isOk())
                    .andExpect(
                            jsonPath("$.data.status")
                                    .value("DRAFT"));
        }

        @Test
        @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
        @DisplayName("Candidate should not access management detail")
        void candidateShouldNotAccessManagementDetail()
                throws Exception {

            Job job = saveJobWithStatus(
                    "Draft Job",
                    "draft-job",
                    openAi,
                    JobStatus.DRAFT);

            mockMvc.perform(
                    get(
                            "/api/jobs/manage/{id}",
                            job.getId()))
                    .andExpect(
                            status().isForbidden());
        }
    }

    @Test
    @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
    @DisplayName("Public API should expose only PUBLISHED jobs")
    void publicApiShouldExposeOnlyPublishedJobs()
            throws Exception {

        saveJobWithStatus(
                "Draft",
                "draft",
                openAi,
                JobStatus.DRAFT);

        saveJobWithStatus(
                "Published",
                "published",
                openAi,
                JobStatus.PUBLISHED);

        saveJobWithStatus(
                "Closed",
                "closed",
                openAi,
                JobStatus.CLOSED);

        saveJobWithStatus(
                "Expired",
                "expired",
                openAi,
                JobStatus.EXPIRED);

        saveJobWithStatus(
                "Archived",
                "archived",
                openAi,
                JobStatus.ARCHIVED);

        mockMvc.perform(
                get("/api/jobs"))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.data.totalElements")
                                .value(1))
                .andExpect(
                        jsonPath(
                                "$.data.items[0].status")
                                .value("PUBLISHED"));
    }

    @Test
    @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
    @DisplayName("Candidate should not change job status")
    void candidateShouldNotChangeJobStatus()
            throws Exception {

        Job job = saveJobWithStatus(
                "Draft Job",
                "draft-job",
                openAi,
                JobStatus.DRAFT);

        mockMvc.perform(
                post(
                        "/api/jobs/{id}/publish",
                        job.getId()))
                .andExpect(
                        status().isForbidden());

        Job unchanged = jobRepository.findById(
                job.getId()).orElseThrow();

        assertThat(unchanged.getStatus())
                .isEqualTo(
                        JobStatus.DRAFT);
    }

    @Test
    @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
    @DisplayName("Should return 404 when changing status of non-existing job")
    void shouldReturn404ForNonExistingJob()
            throws Exception {

        mockMvc.perform(
                post(
                        "/api/jobs/{id}/publish",
                        999999L))
                .andExpect(
                        status().isNotFound())
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "RESOURCE_NOT_FOUND"));
    }
}
