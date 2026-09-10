package com.example.qltd.interview.service;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.service.ApplicationAuthorizationService;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.common.exception.ForbiddenException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.interview.dto.response.InterviewResponse;
import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.mapper.InterviewMapper;
import com.example.qltd.interview.repository.InterviewRepository;
import com.example.qltd.interview.service.impl.InterviewServiceImpl;
import com.example.qltd.interview.support.InterviewTestFactory;
import com.example.qltd.interview.validator.InterviewValidator;
import com.example.qltd.job.entity.Job;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Interview list service")
class InterviewListServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationAuthorizationService authorizationService;

    @Mock
    private InterviewValidator interviewValidator;

    @Mock
    private InterviewStatusService interviewStatusService;

    @Mock
    private InterviewMapper interviewMapper;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private Application application;

    private User recruiter;

    private Interview roundOne;

    private Interview roundTwo;

    @BeforeEach
    void setUp() {

        Company company = ApplicationTestFactory.company();
        company.setId(1L);

        Job job = ApplicationTestFactory.publishedJob(company);
        job.setId(10L);

        User candidate = ApplicationTestFactory.candidate(
                "interview-list-candidate@test.com");
        candidate.setId(20L);

        recruiter = ApplicationTestFactory.recruiter(
                "recruiter@test.com",
                company);
        recruiter.setId(30L);

        application = ApplicationTestFactory.application(
                job,
                candidate,
                ApplicationStatus.INTERVIEW);
        application.setId(40L);

        roundOne = InterviewTestFactory.interview(
                application,
                1,
                LocalDateTime.of(2026, 9, 15, 9, 0));
        roundOne.setId(50L);

        roundTwo = InterviewTestFactory.interview(
                application,
                2,
                LocalDateTime.of(2026, 9, 16, 9, 0));
        roundTwo.setId(51L);
    }

    @Test
    @DisplayName("Should return application interviews in round order")
    void shouldReturnApplicationInterviewsInRoundOrder() {

        InterviewResponse firstResponse = InterviewResponse.builder()
                .id(50L)
                .roundNumber(1)
                .build();
        InterviewResponse secondResponse = InterviewResponse.builder()
                .id(51L)
                .roundNumber(2)
                .build();

        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));
        when(interviewRepository
                .findAllByApplicationIdOrderByRoundNumberAsc(40L))
                .thenReturn(List.of(roundOne, roundTwo));
        when(interviewMapper.toResponse(roundOne))
                .thenReturn(firstResponse);
        when(interviewMapper.toResponse(roundTwo))
                .thenReturn(secondResponse);

        List<InterviewResponse> result = interviewService
                .getApplicationInterviews(
                        40L,
                        "recruiter@test.com");

        assertThat(result)
                .containsExactly(
                        firstResponse,
                        secondResponse);

        verify(authorizationService).checkCanManage(
                application,
                recruiter);
        verify(interviewRepository)
                .findAllByApplicationIdOrderByRoundNumberAsc(40L);
    }

    @Test
    @DisplayName("Should return empty list when application has no interviews")
    void shouldReturnEmptyListWhenApplicationHasNoInterviews() {

        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));
        when(interviewRepository
                .findAllByApplicationIdOrderByRoundNumberAsc(40L))
                .thenReturn(List.of());

        List<InterviewResponse> result = interviewService
                .getApplicationInterviews(
                        40L,
                        "recruiter@test.com");

        assertThat(result).isEmpty();
        verifyNoInteractions(interviewMapper);
    }

    @Test
    @DisplayName("Should reject when application does not exist")
    void shouldRejectWhenApplicationDoesNotExist() {

        when(applicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService
                .getApplicationInterviews(
                        999L,
                        "recruiter@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Application not found.");

        verifyNoInteractions(
                userRepository,
                authorizationService,
                interviewRepository,
                interviewMapper);
    }

    @Test
    @DisplayName("Should reject when manager does not exist")
    void shouldRejectWhenManagerDoesNotExist() {

        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "missing@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService
                .getApplicationInterviews(
                        40L,
                        "missing@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found.");

        verifyNoInteractions(
                authorizationService,
                interviewRepository,
                interviewMapper);
    }

    @Test
    @DisplayName("Should reject unauthorized manager before loading interviews")
    void shouldRejectUnauthorizedManagerBeforeLoadingInterviews() {

        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));
        doThrow(new ForbiddenException(
                "Recruiter cannot manage applications for another company."))
                .when(authorizationService)
                .checkCanManage(
                        application,
                        recruiter);

        assertThatThrownBy(() -> interviewService
                .getApplicationInterviews(
                        40L,
                        "recruiter@test.com"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(
                        "Recruiter cannot manage applications for another company.");

        verify(interviewRepository, never())
                .findAllByApplicationIdOrderByRoundNumberAsc(40L);
        verifyNoInteractions(interviewMapper);
    }
}
