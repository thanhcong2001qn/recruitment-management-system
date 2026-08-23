package com.example.qltd.application.service;

import com.example.qltd.application.dto.request.ChangeApplicationStatusRequest;
import com.example.qltd.application.dto.request.CreateApplicationRequest;
import com.example.qltd.application.dto.response.ApplicationResponse;
import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.mapper.ApplicationMapper;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.service.impl.ApplicationServiceImpl;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.application.validator.ApplicationValidator;
import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.common.exception.DuplicateResourceException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService")
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private ApplicationValidator applicationValidator;

    @Mock
    private ApplicationStatusService applicationStatusService;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private Company company;

    private Job job;

    private User candidate;

    private CreateApplicationRequest createRequest;

    private Application application;

    private ApplicationResponse response;

    @BeforeEach
    void setUp() {

        company = ApplicationTestFactory.company();

        job = ApplicationTestFactory
                .publishedJob(company);

        candidate = ApplicationTestFactory
                .candidate(
                        "candidate@test.com");

        createRequest = ApplicationTestFactory
                .createRequest();

        application = ApplicationTestFactory
                .application(
                        job,
                        candidate);

        response = ApplicationResponse.builder()
                .id(1L)
                .jobId(1L)
                .jobTitle(
                        "Java Backend Developer")
                .candidateId(1L)
                .candidateName(
                        "Test Candidate")
                .candidateEmail(
                        "candidate@test.com")
                .status(
                        ApplicationStatus.APPLIED)
                .build();
    }

    @Nested
    @DisplayName("createApplication()")
    class CreateApplicationTest {

        @Test
        @DisplayName("Should create application successfully")
        void shouldCreateApplicationSuccessfully() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository
                            .findByEmailIgnoreCase(
                                    "candidate@test.com"))
                    .thenReturn(
                            Optional.of(candidate));

            when(
                    applicationRepository
                            .existsByJobIdAndCandidateId(
                                    1L,
                                    candidate.getId()))
                    .thenReturn(false);

            when(
                    applicationRepository.save(
                            any(Application.class)))
                    .thenReturn(application);

            when(
                    applicationMapper.toResponse(
                            application))
                    .thenReturn(response);

            ApplicationResponse result = applicationService
                    .createApplication(
                            1L,
                            "candidate@test.com",
                            createRequest);

            assertThat(result)
                    .isSameAs(response);

            verify(
                    applicationValidator)
                    .validateCreate(
                            job,
                            candidate);

            verify(
                    applicationRepository)
                    .existsByJobIdAndCandidateId(
                            1L,
                            candidate.getId());

            verify(
                    applicationRepository)
                    .save(
                            any(Application.class));

            verify(
                    applicationMapper)
                    .toResponse(
                            application);
        }

        @Test
        @DisplayName("Should throw when job does not exist")
        void shouldThrowWhenJobDoesNotExist() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(999L))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> applicationService
                    .createApplication(
                            999L,
                            "candidate@test.com",
                            createRequest))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Job not found.");

            verifyNoInteractions(
                    userRepository);

            verify(
                    applicationRepository,
                    never())
                    .save(any());
        }

        @Test
        @DisplayName("Should throw when candidate does not exist")
        void shouldThrowWhenCandidateDoesNotExist() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository
                            .findByEmailIgnoreCase(
                                    "missing@test.com"))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> applicationService
                    .createApplication(
                            1L,
                            "missing@test.com",
                            createRequest))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Candidate not found.");

            verify(
                    applicationRepository,
                    never())
                    .save(any());
        }

        @Test
        @DisplayName("Should reject duplicate application")
        void shouldRejectDuplicateApplication() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository
                            .findByEmailIgnoreCase(
                                    "candidate@test.com"))
                    .thenReturn(
                            Optional.of(candidate));

            when(
                    applicationRepository
                            .existsByJobIdAndCandidateId(
                                    1L,
                                    candidate.getId()))
                    .thenReturn(true);

            assertThatThrownBy(() -> applicationService
                    .createApplication(
                            1L,
                            "candidate@test.com",
                            createRequest))
                    .isInstanceOf(
                            DuplicateResourceException.class)
                    .hasMessage(
                            "Candidate has already applied to this job.");

            verify(
                    applicationRepository,
                    never())
                    .save(any());
        }

        @Test
        @DisplayName("Should stop when business validation fails")
        void shouldStopWhenBusinessValidationFails() {

            when(
                    jobRepository
                            .findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository
                            .findByEmailIgnoreCase(
                                    "candidate@test.com"))
                    .thenReturn(
                            Optional.of(candidate));

            doThrow(
                    new BusinessRuleException(
                            "Only published jobs can receive applications."))
                    .when(applicationValidator)
                    .validateCreate(
                            job,
                            candidate);

            assertThatThrownBy(() -> applicationService
                    .createApplication(
                            1L,
                            "candidate@test.com",
                            createRequest))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Only published jobs can receive applications.");

            verify(
                    applicationRepository,
                    never())
                    .existsByJobIdAndCandidateId(
                            any(),
                            any());

            verify(
                    applicationRepository,
                    never())
                    .save(any());
        }
    }

    @Nested
    @DisplayName("getMyApplication()")
    class GetMyApplicationTest {

        @Test
        @DisplayName("Should return own application")
        void shouldReturnOwnApplication() {

            when(
                    userRepository
                            .findByEmailIgnoreCase(
                                    "candidate@test.com"))
                    .thenReturn(
                            Optional.of(candidate));

            when(
                    applicationRepository
                            .findByIdAndCandidateId(
                                    1L,
                                    candidate.getId()))
                    .thenReturn(
                            Optional.of(application));

            when(
                    applicationMapper.toResponse(
                            application))
                    .thenReturn(response);

            ApplicationResponse result = applicationService
                    .getMyApplication(
                            1L,
                            "candidate@test.com");

            assertThat(result)
                    .isSameAs(response);
        }

        @Test
        @DisplayName("Should return not found for another candidate's application")
        void shouldRejectAnotherCandidateApplication() {

            when(
                    userRepository
                            .findByEmailIgnoreCase(
                                    "candidate@test.com"))
                    .thenReturn(
                            Optional.of(candidate));

            when(
                    applicationRepository
                            .findByIdAndCandidateId(
                                    1L,
                                    candidate.getId()))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> applicationService
                    .getMyApplication(
                            1L,
                            "candidate@test.com"))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Application not found.");
        }
    }

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatusTest {

        @Test
        @DisplayName("Should change application status")
        void shouldChangeApplicationStatus() {

            ChangeApplicationStatusRequest request = ApplicationTestFactory
                    .statusRequest(
                            ApplicationStatus.SCREENING);

            when(
                    applicationRepository
                            .findById(1L))
                    .thenReturn(
                            Optional.of(application));

            doAnswer(invocation -> {
                application.setStatus(
                        ApplicationStatus.SCREENING);
                return null;
            })
                    .when(applicationStatusService)
                    .transition(
                            application,
                            ApplicationStatus.SCREENING);

            when(
                    applicationRepository.save(
                            application))
                    .thenReturn(application);

            when(
                    applicationMapper.toResponse(
                            application))
                    .thenReturn(response);

            ApplicationResponse result = applicationService
                    .changeStatus(
                            1L,
                            request);

            assertThat(result)
                    .isSameAs(response);

            assertThat(
                    application.getStatus())
                    .isEqualTo(
                            ApplicationStatus.SCREENING);

            verify(
                    applicationStatusService)
                    .transition(
                            application,
                            ApplicationStatus.SCREENING);

            verify(
                    applicationRepository)
                    .save(application);
        }

        @Test
        @DisplayName("Should reject status change when application does not exist")
        void shouldRejectMissingApplication() {

            ChangeApplicationStatusRequest request = ApplicationTestFactory
                    .statusRequest(
                            ApplicationStatus.SCREENING);

            when(
                    applicationRepository
                            .findById(999L))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> applicationService
                    .changeStatus(
                            999L,
                            request))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Application not found.");

            verify(
                    applicationStatusService,
                    never())
                    .transition(
                            any(),
                            any());
        }
    }

    @Nested
    @DisplayName("withdrawApplication()")
    class WithdrawApplicationTest {

        @Test
        @DisplayName("Should withdraw own application")
        void shouldWithdrawOwnApplication() {

            when(
                    userRepository
                            .findByEmailIgnoreCase(
                                    "candidate@test.com"))
                    .thenReturn(
                            Optional.of(candidate));

            when(
                    applicationRepository
                            .findByIdAndCandidateId(
                                    1L,
                                    candidate.getId()))
                    .thenReturn(
                            Optional.of(application));

            doAnswer(invocation -> {
                application.setStatus(
                        ApplicationStatus.WITHDRAWN);
                return null;
            })
                    .when(applicationStatusService)
                    .transition(
                            application,
                            ApplicationStatus.WITHDRAWN);

            when(
                    applicationMapper.toResponse(
                            application))
                    .thenReturn(response);

            ApplicationResponse result = applicationService
                    .withdrawApplication(
                            1L,
                            "candidate@test.com");

            assertThat(result)
                    .isSameAs(response);

            assertThat(
                    application.getStatus())
                    .isEqualTo(
                            ApplicationStatus.WITHDRAWN);

            verify(
                    applicationStatusService)
                    .transition(
                            application,
                            ApplicationStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("Should reject withdrawing another candidate's application")
        void shouldRejectAnotherCandidateApplication() {

            when(
                    userRepository
                            .findByEmailIgnoreCase(
                                    "candidate@test.com"))
                    .thenReturn(
                            Optional.of(candidate));

            when(
                    applicationRepository
                            .findByIdAndCandidateId(
                                    1L,
                                    candidate.getId()))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> applicationService
                    .withdrawApplication(
                            1L,
                            "candidate@test.com"))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Application not found.");
        }
    }
}