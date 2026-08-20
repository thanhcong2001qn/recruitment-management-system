package com.example.qltd.job.controller;

import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.EmploymentType;
import com.example.qltd.job.enums.ExperienceLevel;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.enums.WorkingType;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.job.support.JobTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Job Controller Integration Test")
class JobControllerIT
        extends AbstractIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company openAi;

    private Company microsoft;

    @BeforeEach
    void setUp() {

        jobRepository.deleteAll();

        companyRepository.deleteAll();

        openAi = companyRepository.save(
                JobTestFactory.activeCompany());

        microsoft = companyRepository.save(
                JobTestFactory
                        .secondActiveCompany());
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
}