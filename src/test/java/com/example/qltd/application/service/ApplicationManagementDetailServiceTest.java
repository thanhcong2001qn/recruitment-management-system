package com.example.qltd.application.service;

import com.example.qltd.application.dto.response.ApplicationResponse;
import com.example.qltd.application.entity.Application;
import com.example.qltd.application.mapper.ApplicationMapper;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.service.impl.ApplicationServiceImpl;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.application.validator.ApplicationValidator;
import com.example.qltd.common.exception.ForbiddenException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.common.mapper.PageMapper;
import com.example.qltd.company.entity.Company;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Application management detail service")
class ApplicationManagementDetailServiceTest {

    @Mock
    private PageMapper pageMapper;

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

    @Mock
    private ApplicationAuthorizationService authorizationService;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private Application application;

    private User recruiter;

    private ApplicationResponse response;

    @BeforeEach
    void setUp() {
        Company company = ApplicationTestFactory.company();
        company.setId(1L);

        Job job = ApplicationTestFactory.publishedJob(company);
        job.setId(10L);

        User candidate = ApplicationTestFactory.candidate(
                "candidate@test.com");
        candidate.setId(20L);

        recruiter = ApplicationTestFactory.recruiter(
                "recruiter@test.com",
                company);
        recruiter.setId(30L);

        application = ApplicationTestFactory.application(
                job,
                candidate);
        application.setId(40L);

        response = ApplicationResponse.builder()
                .id(40L)
                .jobId(10L)
                .candidateId(20L)
                .build();
    }

    @Test
    @DisplayName("Recruiter should get application managed by own company")
    void recruiterShouldGetOwnCompanyApplication() {
        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));
        when(applicationMapper.toResponse(application))
                .thenReturn(response);

        ApplicationResponse result = applicationService
                .getManagementApplication(
                        40L,
                        "recruiter@test.com");

        assertThat(result).isSameAs(response);
        verify(authorizationService).checkCanManage(
                application,
                recruiter);
        verify(applicationMapper).toResponse(application);
    }

    @Test
    @DisplayName("Authorization failure should stop response mapping")
    void authorizationFailureShouldStopMapping() {
        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));

        doThrow(new ForbiddenException(
                "Recruiter cannot manage applications for another company."))
                .when(authorizationService)
                .checkCanManage(application, recruiter);

        assertThatThrownBy(() -> applicationService
                .getManagementApplication(
                        40L,
                        "recruiter@test.com"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(
                        "Recruiter cannot manage applications for another company.");

        verify(applicationMapper, never()).toResponse(application);
    }

    @Test
    @DisplayName("Missing application should return not found")
    void missingApplicationShouldReturnNotFound() {
        when(applicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService
                .getManagementApplication(
                        999L,
                        "recruiter@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Application not found.");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(authorizationService);
    }

    @Test
    @DisplayName("Missing manager should return not found")
    void missingManagerShouldReturnNotFound() {
        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "missing@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService
                .getManagementApplication(
                        40L,
                        "missing@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found.");

        verifyNoInteractions(authorizationService);
        verify(applicationMapper, never()).toResponse(application);
    }
}
