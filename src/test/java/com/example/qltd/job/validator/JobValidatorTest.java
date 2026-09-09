package com.example.qltd.job.validator;

import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.support.JobTestFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JobValidator")
class JobValidatorTest {

    private JobValidator jobValidator;

    private Company activeCompany;
    private Company deletedCompany;

    @BeforeEach
    void setUp() {

        jobValidator = new JobValidator();

        activeCompany = Company.builder()
                .id(1L)
                .name("OpenAI")
                .slug("openai")
                .deleted(false)
                .build();

        deletedCompany = Company.builder()
                .id(2L)
                .name("Deleted Company")
                .slug("deleted-company")
                .deleted(true)
                .build();
    }

    @Nested
    @DisplayName("validateCreate()")
    class ValidateCreateTest {

        private CreateJobRequest validRequest;

        @BeforeEach
        void setUpRequest() {

            validRequest = new CreateJobRequest();

            validRequest.setTitle("Java Backend Developer");
            validRequest.setSalaryMin(
                    new BigDecimal("15000000"));
            validRequest.setSalaryMax(
                    new BigDecimal("30000000"));
            validRequest.setDeadline(
                    LocalDate.now().plusDays(30));
        }

        @Test
        @DisplayName("Should pass when create request is valid")
        void shouldPassWhenRequestIsValid() {

            assertThatCode(() -> jobValidator.validateCreate(
                    validRequest,
                    activeCompany)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject when minimum salary is negative")
        void shouldRejectWhenMinimumSalaryIsNegative() {

            validRequest.setSalaryMin(
                    new BigDecimal("-1"));

            assertThatThrownBy(() -> jobValidator.validateCreate(
                    validRequest,
                    activeCompany))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Minimum salary must not be negative.");
        }

        @Test
        @DisplayName("Should reject when maximum salary is negative")
        void shouldRejectWhenMaximumSalaryIsNegative() {

            validRequest.setSalaryMax(
                    new BigDecimal("-1"));

            assertThatThrownBy(() -> jobValidator.validateCreate(
                    validRequest,
                    activeCompany))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Maximum salary must not be negative.");
        }

        @Test
        @DisplayName("Should reject when minimum salary is greater than maximum salary")
        void shouldRejectWhenMinimumSalaryGreaterThanMaximumSalary() {

            validRequest.setSalaryMin(
                    new BigDecimal("30000000"));

            validRequest.setSalaryMax(
                    new BigDecimal("15000000"));

            assertThatThrownBy(() -> jobValidator.validateCreate(
                    validRequest,
                    activeCompany))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Minimum salary must not be greater than maximum salary.");
        }

        @Test
        @DisplayName("Should accept when salary boundaries are equal")
        void shouldAcceptWhenSalaryBoundariesAreEqual() {

            validRequest.setSalaryMin(
                    new BigDecimal("20000000"));

            validRequest.setSalaryMax(
                    new BigDecimal("20000000"));

            assertThatCode(() -> jobValidator.validateCreate(
                    validRequest,
                    activeCompany)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should accept when salary is not provided")
        void shouldAcceptWhenSalaryIsNotProvided() {

            validRequest.setSalaryMin(null);
            validRequest.setSalaryMax(null);

            assertThatCode(() -> jobValidator.validateCreate(
                    validRequest,
                    activeCompany)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject when deadline is in the past")
        void shouldRejectWhenDeadlineIsInThePast() {

            validRequest.setDeadline(
                    LocalDate.now().minusDays(1));

            assertThatThrownBy(() -> jobValidator.validateCreate(
                    validRequest,
                    activeCompany))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Job deadline must not be in the past.");
        }

        @Test
        @DisplayName("Should accept deadline today")
        void shouldAcceptDeadlineToday() {

            validRequest.setDeadline(
                    LocalDate.now());

            assertThatCode(() -> jobValidator.validateCreate(
                    validRequest,
                    activeCompany)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject when company is null")
        void shouldRejectWhenCompanyIsNull() {

            assertThatThrownBy(() -> jobValidator.validateCreate(
                    validRequest,
                    null))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Company is required.");
        }

        @Test
        @DisplayName("Should reject when company is deleted")
        void shouldRejectWhenCompanyIsDeleted() {

            assertThatThrownBy(() -> jobValidator.validateCreate(
                    validRequest,
                    deletedCompany))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Cannot create job for a deleted company.");
        }
    }

    @Nested
    @DisplayName("validateUpdate()")
    class ValidateUpdateTest {

        private Job existingJob;
        private UpdateJobRequest updateRequest;

        @BeforeEach
        void setUpRequest() {

            existingJob = Job.builder()
                    .id(1L)
                    .title("Java Developer")
                    .salaryMin(
                            new BigDecimal("15000000"))
                    .salaryMax(
                            new BigDecimal("30000000"))
                    .deadline(
                            LocalDate.now().plusDays(30))
                    .status(JobStatus.DRAFT)
                    .build();

            updateRequest = new UpdateJobRequest();
        }

        @Test
        @DisplayName("Should pass when update request is valid")
        void shouldPassWhenUpdateIsValid() {

            updateRequest.setSalaryMin(
                    new BigDecimal("20000000"));

            updateRequest.setSalaryMax(
                    new BigDecimal("35000000"));

            updateRequest.setDeadline(
                    LocalDate.now().plusDays(60));

            assertThatCode(() -> jobValidator.validateUpdate(
                    existingJob,
                    updateRequest)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should use existing salary when only minimum salary is updated")
        void shouldUseExistingMaximumSalaryWhenOnlyMinimumSalaryUpdated() {

            updateRequest.setSalaryMin(
                    new BigDecimal("25000000"));

            assertThatCode(() -> jobValidator.validateUpdate(
                    existingJob,
                    updateRequest)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should use existing minimum salary when only maximum salary is updated")
        void shouldUseExistingMinimumSalaryWhenOnlyMaximumSalaryUpdated() {

            updateRequest.setSalaryMax(
                    new BigDecimal("25000000"));

            assertThatCode(() -> jobValidator.validateUpdate(
                    existingJob,
                    updateRequest)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject invalid salary range during update")
        void shouldRejectInvalidSalaryRange() {

            updateRequest.setSalaryMin(
                    new BigDecimal("40000000"));

            updateRequest.setSalaryMax(
                    new BigDecimal("20000000"));

            assertThatThrownBy(() -> jobValidator.validateUpdate(
                    existingJob,
                    updateRequest))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Minimum salary must not be greater than maximum salary.");
        }

        @Test
        @DisplayName("Should reject past deadline during update")
        void shouldRejectPastDeadline() {

            updateRequest.setDeadline(
                    LocalDate.now().minusDays(1));

            assertThatThrownBy(() -> jobValidator.validateUpdate(
                    existingJob,
                    updateRequest))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Job deadline must not be in the past.");
        }

        @Test
        @DisplayName("Should skip salary validation when salary is not updated")
        void shouldSkipSalaryValidationWhenSalaryIsNotUpdated() {

            updateRequest.setDeadline(
                    LocalDate.now().plusDays(10));

            assertThatCode(() -> jobValidator.validateUpdate(
                    existingJob,
                    updateRequest)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validatePublish()")
    class ValidatePublishTest {

        private Job job;

        @BeforeEach
        void setUpJob() {

            job = Job.builder()
                    .id(1L)
                    .title("Java Developer")
                    .status(JobStatus.DRAFT)
                    .deadline(
                            LocalDate.now().plusDays(30))
                    .build();
        }

        @Test
        @DisplayName("Should pass when DRAFT job has valid deadline")
        void shouldPassWhenDraftJobCanBePublished() {

            assertThatCode(() -> jobValidator.validatePublish(job)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject publishing non-DRAFT job")
        void shouldRejectPublishingNonDraftJob() {

            job.setStatus(JobStatus.PUBLISHED);

            assertThatThrownBy(() -> jobValidator.validatePublish(job))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Only DRAFT jobs can be published.");
        }

        @Test
        @DisplayName("Should reject publishing job without deadline")
        void shouldRejectPublishingJobWithoutDeadline() {

            job.setDeadline(null);

            assertThatThrownBy(() -> jobValidator.validatePublish(job))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Job deadline is required before publishing.");
        }

        @Test
        @DisplayName("Should reject publishing job with past deadline")
        void shouldRejectPublishingJobWithPastDeadline() {

            job.setDeadline(
                    LocalDate.now().minusDays(1));

            assertThatThrownBy(() -> jobValidator.validatePublish(job))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Job deadline must not be in the past.");
        }

        @Test
        @DisplayName("Should accept publishing job with today's deadline")
        void shouldAcceptPublishingJobWithTodayDeadline() {

            job.setDeadline(LocalDate.now());

            assertThatCode(() -> jobValidator.validatePublish(job)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validateDelete()")
    class ValidateDeleteTest {

        @Test
        @DisplayName("Should allow deleting DRAFT job")
        void shouldAllowDeletingDraftJob() {

            Job job = JobTestFactory.draftJob(
                    activeCompany);

            assertThatCode(() -> jobValidator.validateDelete(job)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject deleting PUBLISHED job")
        void shouldRejectDeletingPublishedJob() {

            Job job = JobTestFactory.job(
                    activeCompany);

            job.setStatus(
                    JobStatus.PUBLISHED);

            assertThatThrownBy(() -> jobValidator.validateDelete(job))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Published job must be closed before deletion.");
        }

        @Test
        @DisplayName("Should allow deleting CLOSED job")
        void shouldAllowDeletingClosedJob() {

            Job job = JobTestFactory.closedJob(
                    activeCompany);

            assertThatCode(() -> jobValidator.validateDelete(job)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow deleting EXPIRED job")
        void shouldAllowDeletingExpiredJob() {

            Job job = JobTestFactory.expiredJob(
                    activeCompany);

            assertThatCode(() -> jobValidator.validateDelete(job)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject deleting ARCHIVED job")
        void shouldRejectDeletingArchivedJob() {

            Job job = JobTestFactory.job(
                    activeCompany);

            job.setStatus(
                    JobStatus.ARCHIVED);

            assertThatThrownBy(() -> jobValidator.validateDelete(job))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Archived job cannot be deleted.");
        }
    }
}