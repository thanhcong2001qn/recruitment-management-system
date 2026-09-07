package com.example.qltd.application.service;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.service.impl.ApplicationAuthorizationServiceImpl;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.common.exception.ForbiddenException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationAuthorizationService")
class ApplicationAuthorizationServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ApplicationAuthorizationServiceImpl authorizationService;

    private Company companyA;

    private Company companyB;

    private Job jobA;

    private Job jobB;

    private User recruiterA;

    private User recruiterB;

    private User admin;

    private Application applicationA;

    @BeforeEach
    void setUp() {

        companyA = Company.builder()
                .id(1L)
                .name("Company A")
                .slug("company-a")
                .deleted(false)
                .build();

        companyB = Company.builder()
                .id(2L)
                .name("Company B")
                .slug("company-b")
                .deleted(false)
                .build();

        jobA = ApplicationTestFactory
                .publishedJob(companyA);

        jobA.setId(10L);

        jobB = ApplicationTestFactory
                .publishedJob(companyB);

        jobB.setId(20L);

        recruiterA = ApplicationTestFactory
                .recruiter(
                        "recruiter-a@test.com",
                        companyA);

        recruiterB = ApplicationTestFactory
                .recruiter(
                        "recruiter-b@test.com",
                        companyB);

        admin = ApplicationTestFactory
                .admin(
                        "admin@test.com");

        applicationA = ApplicationTestFactory
                .application(
                        jobA,
                        ApplicationTestFactory
                                .candidate(
                                        "candidate@test.com"));
    }

    @Nested
    @DisplayName("checkCanManage()")
    class CheckCanManageTest {

        @Test
        @DisplayName("Should allow recruiter to manage application of own company")
        void shouldAllowRecruiterOfSameCompany() {

            assertThatCode(() -> authorizationService.checkCanManage(
                    applicationA,
                    recruiterA))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject recruiter from another company")
        void shouldRejectRecruiterOfAnotherCompany() {

            assertThatThrownBy(() -> authorizationService.checkCanManage(
                    applicationA,
                    recruiterB))
                    .isInstanceOf(
                            ForbiddenException.class)
                    .hasMessage(
                            "Recruiter cannot manage applications for another company.");
        }

        @Test
        @DisplayName("Should allow admin to manage any application")
        void shouldAllowAdmin() {

            assertThatCode(() -> authorizationService.checkCanManage(
                    applicationA,
                    admin))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject null application")
        void shouldRejectNullApplication() {

            assertThatThrownBy(() -> authorizationService.checkCanManage(
                    null,
                    recruiterA))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Application not found.");
        }

        @Test
        @DisplayName("Should reject null user")
        void shouldRejectNullUser() {

            assertThatThrownBy(() -> authorizationService.checkCanManage(
                    applicationA,
                    null))
                    .isInstanceOf(
                            ForbiddenException.class)
                    .hasMessage(
                            "User is required.");
        }

        @Test
        @DisplayName("Should reject recruiter without company")
        void shouldRejectRecruiterWithoutCompany() {

            User recruiter = ApplicationTestFactory
                    .recruiter(
                            "orphan@test.com",
                            null);

            assertThatThrownBy(() -> authorizationService.checkCanManage(
                    applicationA,
                    recruiter))
                    .isInstanceOf(
                            ForbiddenException.class)
                    .hasMessage(
                            "Recruiter is not assigned to a company.");
        }

        @Test
        @DisplayName("Should reject candidate from management operation")
        void shouldRejectCandidate() {

            User candidate = ApplicationTestFactory
                    .candidate(
                            "candidate@test.com");

            assertThatThrownBy(() -> authorizationService.checkCanManage(
                    applicationA,
                    candidate))
                    .isInstanceOf(
                            ForbiddenException.class)
                    .hasMessage(
                            "User is not allowed to manage applications.");
        }
    }

    @Nested
    @DisplayName("checkCanManageJob()")
    class CheckCanManageJobTest {

        @Test
        @DisplayName("Should allow recruiter to manage own company job")
        void shouldAllowRecruiterOwnCompanyJob() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(10L))
                    .thenReturn(
                            Optional.of(jobA));

            assertThatCode(() -> authorizationService.checkCanManageJob(
                    10L,
                    recruiterA))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject recruiter from another company job")
        void shouldRejectRecruiterAnotherCompanyJob() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(20L))
                    .thenReturn(
                            Optional.of(jobB));

            assertThatThrownBy(() -> authorizationService.checkCanManageJob(
                    20L,
                    recruiterA))
                    .isInstanceOf(
                            ForbiddenException.class)
                    .hasMessage(
                            "Recruiter cannot manage applications for another company.");
        }

        @Test
        @DisplayName("Should allow admin to manage any company job")
        void shouldAllowAdminAnyJob() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(20L))
                    .thenReturn(
                            Optional.of(jobB));

            assertThatCode(() -> authorizationService.checkCanManageJob(
                    20L,
                    admin))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject missing job")
        void shouldRejectMissingJob() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(999L))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> authorizationService.checkCanManageJob(
                    999L,
                    recruiterA))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Job not found.");
        }

        @Test
        @DisplayName("Should reject recruiter without company")
        void shouldRejectRecruiterWithoutCompany() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(10L))
                    .thenReturn(
                            Optional.of(jobA));

            User recruiter = ApplicationTestFactory
                    .recruiter(
                            "orphan@test.com",
                            null);

            assertThatThrownBy(() -> authorizationService.checkCanManageJob(
                    10L,
                    recruiter))
                    .isInstanceOf(
                            ForbiddenException.class)
                    .hasMessage(
                            "Recruiter is not assigned to a company.");
        }

        @Test
        @DisplayName("Should reject candidate")
        void shouldRejectCandidate() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(10L))
                    .thenReturn(
                            Optional.of(jobA));

            User candidate = ApplicationTestFactory
                    .candidate(
                            "candidate@test.com");

            assertThatThrownBy(() -> authorizationService.checkCanManageJob(
                    10L,
                    candidate))
                    .isInstanceOf(
                            ForbiddenException.class)
                    .hasMessage(
                            "User is not allowed to manage applications.");
        }
    }
}