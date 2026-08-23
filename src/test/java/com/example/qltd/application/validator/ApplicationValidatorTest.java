package com.example.qltd.application.validator;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ApplicationValidator")
class ApplicationValidatorTest {

    private ApplicationValidator validator;

    private Company company;

    private Job publishedJob;

    private User candidate;

    @BeforeEach
    void setUp() {

        validator = new ApplicationValidator();

        company = ApplicationTestFactory.company();

        publishedJob = ApplicationTestFactory
                .publishedJob(company);

        candidate = ApplicationTestFactory
                .candidate("candidate@test.com");
    }

    @Nested
    @DisplayName("validateCreate()")
    class ValidateCreateTest {

        @Test
        @DisplayName("Should pass for valid published job and active candidate")
        void shouldPassForValidApplication() {

            assertThatCode(() -> validator.validateCreate(
                    publishedJob,
                    candidate))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject null job")
        void shouldRejectNullJob() {

            assertThatThrownBy(() -> validator.validateCreate(
                    null,
                    candidate))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Job is required.");
        }

        @Test
        @DisplayName("Should reject deleted job")
        void shouldRejectDeletedJob() {

            publishedJob.setDeleted(true);

            assertThatThrownBy(() -> validator.validateCreate(
                    publishedJob,
                    candidate))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Cannot apply to a deleted job.");
        }

        @Test
        @DisplayName("Should reject draft job")
        void shouldRejectDraftJob() {

            publishedJob.setStatus(
                    JobStatus.DRAFT);

            assertThatThrownBy(() -> validator.validateCreate(
                    publishedJob,
                    candidate))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Only published jobs can receive applications.");
        }

        @Test
        @DisplayName("Should reject closed job")
        void shouldRejectClosedJob() {

            publishedJob.setStatus(
                    JobStatus.CLOSED);

            assertThatThrownBy(() -> validator.validateCreate(
                    publishedJob,
                    candidate))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Only published jobs can receive applications.");
        }

        @Test
        @DisplayName("Should reject null candidate")
        void shouldRejectNullCandidate() {

            assertThatThrownBy(() -> validator.validateCreate(
                    publishedJob,
                    null))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Candidate is required.");
        }

        @Test
        @DisplayName("Should reject recruiter as applicant")
        void shouldRejectRecruiter() {

            User recruiter = ApplicationTestFactory
                    .recruiterAsCandidate(
                            "recruiter@test.com");

            assertThatThrownBy(() -> validator.validateCreate(
                    publishedJob,
                    recruiter))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Only candidates can apply for jobs.");
        }

        @Test
        @DisplayName("Should reject inactive candidate")
        void shouldRejectInactiveCandidate() {

            User inactive = ApplicationTestFactory
                    .inactiveCandidate(
                            "inactive@test.com");

            assertThatThrownBy(() -> validator.validateCreate(
                    publishedJob,
                    inactive))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Candidate account is not active.");
        }
    }

    @Nested
    @DisplayName("validateStatusTransition()")
    class ValidateStatusTransitionTest {

        @Test
        @DisplayName("APPLIED -> SCREENING should be valid")
        void appliedToScreening() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.APPLIED);

            assertThatCode(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.SCREENING))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("APPLIED -> REJECTED should be valid")
        void appliedToRejected() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.APPLIED);

            assertThatCode(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.REJECTED))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("APPLIED -> WITHDRAWN should be valid")
        void appliedToWithdrawn() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.APPLIED);

            assertThatCode(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.WITHDRAWN))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("APPLIED -> HIRED should be invalid")
        void appliedToHiredShouldFail() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.APPLIED);

            assertThatThrownBy(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.HIRED))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Invalid application status transition: APPLIED -> HIRED");
        }

        @Test
        @DisplayName("SCREENING -> SHORTLISTED should be valid")
        void screeningToShortlisted() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.SCREENING);

            assertThatCode(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.SHORTLISTED))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("SHORTLISTED -> INTERVIEW should be valid")
        void shortlistedToInterview() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.SHORTLISTED);

            assertThatCode(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.INTERVIEW))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("INTERVIEW -> OFFERED should be valid")
        void interviewToOffered() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.INTERVIEW);

            assertThatCode(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.OFFERED))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("OFFERED -> HIRED should be valid")
        void offeredToHired() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.OFFERED);

            assertThatCode(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.HIRED))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("OFFERED -> REJECTED should be valid")
        void offeredToRejected() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.OFFERED);

            assertThatCode(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.REJECTED))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("HIRED -> APPLIED should be invalid")
        void hiredToAppliedShouldFail() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.HIRED);

            assertThatThrownBy(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.APPLIED))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Application is already in a terminal status.");
        }

        @Test
        @DisplayName("REJECTED -> SCREENING should be invalid")
        void rejectedToScreeningShouldFail() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.REJECTED);

            assertThatThrownBy(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.SCREENING))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Application is already in a terminal status.");
        }

        @Test
        @DisplayName("WITHDRAWN -> SCREENING should be invalid")
        void withdrawnToScreeningShouldFail() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.WITHDRAWN);

            assertThatThrownBy(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.SCREENING))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Application is already in a terminal status.");
        }

        @Test
        @DisplayName("Null application should be rejected")
        void nullApplicationShouldFail() {

            assertThatThrownBy(() -> validator.validateStatusTransition(
                    null,
                    ApplicationStatus.SCREENING))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Application is required.");
        }

        @Test
        @DisplayName("Null target status should be rejected")
        void nullTargetStatusShouldFail() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate);

            assertThatThrownBy(() -> validator.validateStatusTransition(
                    application,
                    null))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Target status is required.");
        }

        @Test
        @DisplayName("Same status should be allowed")
        void sameStatusShouldBeAllowed() {

            Application application = ApplicationTestFactory
                    .application(
                            publishedJob,
                            candidate,
                            ApplicationStatus.SCREENING);

            assertThatCode(() -> validator.validateStatusTransition(
                    application,
                    ApplicationStatus.SCREENING))
                    .doesNotThrowAnyException();
        }
    }
}