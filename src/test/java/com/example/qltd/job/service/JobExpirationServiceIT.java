package com.example.qltd.job.service;

import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.config.TestTimeConfig;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.EmploymentType;
import com.example.qltd.job.enums.ExperienceLevel;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.enums.WorkingType;
import com.example.qltd.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestTimeConfig.class)
@DisplayName("Job Expiration Integration Test")
@TestPropertySource(properties = {
        "job.expiration.cron=0 0 0 * * *"
})
class JobExpirationServiceIT
        extends AbstractIntegrationTest {

    @Autowired
    private JobExpirationService jobExpirationService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company company;

    @BeforeEach
    void setUp() {

        jobRepository.deleteAll();

        companyRepository.deleteAll();

        company = Company.builder()
                .name("OpenAI")
                .slug("openai")
                .email("contact@openai.com")
                .deleted(false)
                .build();

        company = companyRepository.save(company);
    }

    @Test
    @DisplayName("Should expire overdue published jobs")
    void shouldExpireOverduePublishedJobs() {

        Job overdueJob = Job.builder()
                .title("Java Developer")
                .slug("java-developer")
                .status(JobStatus.PUBLISHED)
                .deadline(
                        LocalDate.of(2026, 8, 21)
                                .minusDays(1))
                .deleted(false)
                .company(company)
                .employmentType(EmploymentType.FULL_TIME)
                .experienceLevel(ExperienceLevel.JUNIOR)
                .workingType(WorkingType.HYBRID)
                .build();

        overdueJob = jobRepository.save(overdueJob);

        int result = jobExpirationService.expireJobs();

        assertThat(result)
                .isEqualTo(1);

        Job updated = jobRepository.findById(
                overdueJob.getId()).orElseThrow();

        assertThat(updated.getStatus())
                .isEqualTo(
                        JobStatus.EXPIRED);
    }

    @Test
    @DisplayName("Should not expire job whose deadline is today")
    void shouldNotExpireJobDueToday() {

        Job job = Job.builder()
                .title("Today Developer")
                .slug("today-developer")
                .status(JobStatus.PUBLISHED)
                .deadline(
                        LocalDate.of(2026, 8, 21))
                .deleted(false)
                .company(company)
                .employmentType(EmploymentType.FULL_TIME)
                .experienceLevel(ExperienceLevel.JUNIOR)
                .workingType(WorkingType.HYBRID)
                .build();

        job = jobRepository.save(job);

        int result = jobExpirationService.expireJobs();

        assertThat(result)
                .isZero();

        Job unchanged = jobRepository.findById(
                job.getId()).orElseThrow();

        assertThat(unchanged.getStatus())
                .isEqualTo(
                        JobStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should not expire DRAFT job")
    void shouldNotExpireDraftJob() {

        Job job = Job.builder()
                .title("Draft Developer")
                .slug("draft-developer")
                .status(JobStatus.DRAFT)
                .deadline(
                        LocalDate.of(2026, 8, 21)
                                .minusDays(10))
                .deleted(false)
                .company(company)
                .employmentType(EmploymentType.FULL_TIME)
                .experienceLevel(ExperienceLevel.JUNIOR)
                .workingType(WorkingType.HYBRID)
                .build();

        job = jobRepository.save(job);

        int result = jobExpirationService.expireJobs();

        assertThat(result)
                .isZero();

        Job unchanged = jobRepository.findById(
                job.getId()).orElseThrow();

        assertThat(unchanged.getStatus())
                .isEqualTo(
                        JobStatus.DRAFT);
    }

    @Test
    @DisplayName("Should not expire soft deleted job")
    void shouldNotExpireDeletedJob() {

        Job job = Job.builder()
                .title("Deleted Developer")
                .slug("deleted-developer")
                .status(JobStatus.PUBLISHED)
                .deadline(
                        LocalDate.of(2026, 8, 21)
                                .minusDays(10))
                .deleted(true)
                .company(company)
                .employmentType(EmploymentType.FULL_TIME)
                .experienceLevel(ExperienceLevel.JUNIOR)
                .workingType(WorkingType.HYBRID)
                .build();

        job = jobRepository.save(job);

        int result = jobExpirationService.expireJobs();

        assertThat(result)
                .isZero();

        Job unchanged = jobRepository.findById(
                job.getId()).orElseThrow();

        assertThat(unchanged.getStatus())
                .isEqualTo(
                        JobStatus.PUBLISHED);

        assertThat(unchanged.getDeleted())
                .isTrue();
    }
}