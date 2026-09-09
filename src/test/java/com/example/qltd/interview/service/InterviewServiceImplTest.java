package com.example.qltd.interview.service;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.service.ApplicationAuthorizationService;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.common.exception.DuplicateResourceException;
import com.example.qltd.common.exception.ForbiddenException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.interview.dto.request.ChangeInterviewStatusRequest;
import com.example.qltd.interview.dto.request.CreateInterviewRequest;
import com.example.qltd.interview.dto.response.InterviewResponse;
import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.enums.InterviewStatus;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewService")
class InterviewServiceImplTest {

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

    private CreateInterviewRequest request;

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
                candidate,
                ApplicationStatus.INTERVIEW);
        application.setId(40L);

        request = InterviewTestFactory.createRequest(
                LocalDateTime.of(2026, 9, 15, 9, 0));
    }

    @Test
    @DisplayName("Should schedule interview successfully")
    void shouldScheduleInterviewSuccessfully() {

        InterviewResponse expected = InterviewResponse.builder()
                .id(50L)
                .applicationId(40L)
                .roundNumber(1)
                .build();

        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));
        when(interviewRepository
                .existsByApplicationIdAndRoundNumber(40L, 1))
                .thenReturn(false);
        when(interviewRepository.save(
                any(Interview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(interviewMapper.toResponse(
                any(Interview.class)))
                .thenReturn(expected);

        InterviewResponse result = interviewService.scheduleInterview(
                40L,
                "recruiter@test.com",
                request);

        assertThat(result).isSameAs(expected);

        ArgumentCaptor<Interview> captor = ArgumentCaptor.forClass(
                Interview.class);

        verify(interviewRepository).save(captor.capture());

        Interview saved = captor.getValue();
        assertThat(saved.getApplication()).isSameAs(application);
        assertThat(saved.getRoundNumber()).isEqualTo(1);
        assertThat(saved.getInterviewType())
                .isEqualTo(request.getInterviewType());
        assertThat(saved.getScheduledAt())
                .isEqualTo(request.getScheduledAt());
        assertThat(saved.getDurationMinutes()).isEqualTo(60);

        verify(authorizationService).checkCanManage(
                application,
                recruiter);
        verify(interviewValidator).validateSchedule(
                application,
                request);
    }

    @Test
    @DisplayName("Missing manager should return not found")
    void missingManagerShouldReturnNotFound() {

        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "missing@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.scheduleInterview(
                40L,
                "missing@test.com",
                request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found.");

        verifyNoInteractions(authorizationService);
        verifyNoInteractions(interviewRepository);
    }

    @Test
    @DisplayName("Missing application should return not found")
    void missingApplicationShouldReturnNotFound() {

        when(applicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.scheduleInterview(
                999L,
                "recruiter@test.com",
                request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Application not found.");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(interviewRepository);
    }

    @Test
    @DisplayName("Authorization failure should stop interview creation")
    void authorizationFailureShouldStopInterviewCreation() {

        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));

        doThrow(new ForbiddenException(
                "Recruiter cannot manage applications for another company."))
                .when(authorizationService)
                .checkCanManage(application, recruiter);

        assertThatThrownBy(() -> interviewService.scheduleInterview(
                40L,
                "recruiter@test.com",
                request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(
                        "Recruiter cannot manage applications for another company.");

        verifyNoInteractions(interviewValidator);
        verifyNoInteractions(interviewRepository);
    }

    @Test
    @DisplayName("Duplicate round should return conflict")
    void duplicateRoundShouldReturnConflict() {

        when(applicationRepository.findById(40L))
                .thenReturn(Optional.of(application));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));
        when(interviewRepository
                .existsByApplicationIdAndRoundNumber(40L, 1))
                .thenReturn(true);

        assertThatThrownBy(() -> interviewService.scheduleInterview(
                40L,
                "recruiter@test.com",
                request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(
                        "Interview round already exists for this application.");

        verify(interviewValidator).validateSchedule(
                application,
                request);
        verify(interviewRepository, never()).save(
                any(Interview.class));
        verifyNoInteractions(interviewMapper);
    }

    @Test
    @DisplayName("Should change interview status successfully")
    void shouldChangeInterviewStatusSuccessfully() {

        Interview interview = InterviewTestFactory.interview(
                application,
                1,
                request.getScheduledAt());
        interview.setId(50L);

        ChangeInterviewStatusRequest statusRequest = statusRequest(
                InterviewStatus.IN_PROGRESS);

        InterviewResponse expected = InterviewResponse.builder()
                .id(50L)
                .status(InterviewStatus.IN_PROGRESS)
                .build();

        when(interviewRepository.findById(50L))
                .thenReturn(Optional.of(interview));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));
        when(interviewRepository.save(interview))
                .thenReturn(interview);
        when(interviewMapper.toResponse(interview))
                .thenReturn(expected);

        InterviewResponse result = interviewService.changeStatus(
                50L,
                "recruiter@test.com",
                statusRequest);

        assertThat(result).isSameAs(expected);
        verify(authorizationService).checkCanManage(
                application,
                recruiter);
        verify(interviewStatusService).transition(
                interview,
                InterviewStatus.IN_PROGRESS);
        verify(interviewRepository).save(interview);
    }

    @Test
    @DisplayName("Missing interview should return not found")
    void missingInterviewShouldReturnNotFound() {

        ChangeInterviewStatusRequest statusRequest = statusRequest(
                InterviewStatus.IN_PROGRESS);

        when(interviewRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.changeStatus(
                999L,
                "recruiter@test.com",
                statusRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Interview not found.");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(interviewStatusService);
    }

    @Test
    @DisplayName("Status authorization failure should stop update")
    void statusAuthorizationFailureShouldStopUpdate() {

        Interview interview = InterviewTestFactory.interview(
                application,
                1,
                request.getScheduledAt());
        interview.setId(50L);

        ChangeInterviewStatusRequest statusRequest = statusRequest(
                InterviewStatus.IN_PROGRESS);

        when(interviewRepository.findById(50L))
                .thenReturn(Optional.of(interview));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));

        doThrow(new ForbiddenException(
                "Recruiter cannot manage applications for another company."))
                .when(authorizationService)
                .checkCanManage(application, recruiter);

        assertThatThrownBy(() -> interviewService.changeStatus(
                50L,
                "recruiter@test.com",
                statusRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(
                        "Recruiter cannot manage applications for another company.");

        verifyNoInteractions(interviewStatusService);
        verify(interviewRepository, never()).save(interview);
        verifyNoInteractions(interviewMapper);
    }

    private ChangeInterviewStatusRequest statusRequest(
            InterviewStatus status) {

        ChangeInterviewStatusRequest statusRequest = new ChangeInterviewStatusRequest();
        statusRequest.setStatus(status);

        return statusRequest;
    }
}
