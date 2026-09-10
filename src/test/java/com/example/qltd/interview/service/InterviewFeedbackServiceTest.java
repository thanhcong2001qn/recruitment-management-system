package com.example.qltd.interview.service;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.service.ApplicationAuthorizationService;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.common.exception.ForbiddenException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.interview.dto.request.UpdateInterviewFeedbackRequest;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Interview feedback service")
class InterviewFeedbackServiceTest {

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

    private Interview interview;

    private User recruiter;

    private UpdateInterviewFeedbackRequest request;

    @BeforeEach
    void setUp() {

        Company company = ApplicationTestFactory.company();
        company.setId(1L);

        Job job = ApplicationTestFactory.publishedJob(company);
        job.setId(10L);

        User candidate = ApplicationTestFactory.candidate(
                "feedback-candidate@test.com");
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

        interview = InterviewTestFactory.interview(
                application,
                1,
                LocalDateTime.of(2026, 9, 15, 9, 0));
        interview.setId(50L);
        interview.setStatus(InterviewStatus.COMPLETED);

        request = new UpdateInterviewFeedbackRequest();
        request.setFeedback(
                "Strong Java fundamentals and clear communication.");
        request.setRating(5);
    }

    @Test
    @DisplayName("Should update feedback successfully")
    void shouldUpdateFeedbackSuccessfully() {

        interview.setFeedback("Previous feedback.");
        interview.setRating(3);

        InterviewResponse expected = InterviewResponse.builder()
                .id(50L)
                .status(InterviewStatus.COMPLETED)
                .feedback(request.getFeedback())
                .rating(5)
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

        InterviewResponse result = interviewService.updateFeedback(
                50L,
                "recruiter@test.com",
                request);

        assertThat(result).isSameAs(expected);
        assertThat(interview.getFeedback())
                .isEqualTo(request.getFeedback());
        assertThat(interview.getRating()).isEqualTo(5);

        verify(authorizationService).checkCanManage(
                application,
                recruiter);
        verify(interviewValidator).validateFeedback(interview);
        verify(interviewRepository).save(interview);
        verify(interviewMapper).toResponse(interview);
    }

    @Test
    @DisplayName("Missing interview should return not found")
    void missingInterviewShouldReturnNotFound() {

        when(interviewRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.updateFeedback(
                999L,
                "recruiter@test.com",
                request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Interview not found.");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(authorizationService);
        verifyNoInteractions(interviewValidator);
    }

    @Test
    @DisplayName("Missing manager should return not found")
    void missingManagerShouldReturnNotFound() {

        when(interviewRepository.findById(50L))
                .thenReturn(Optional.of(interview));
        when(userRepository.findByEmailIgnoreCase(
                "missing@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.updateFeedback(
                50L,
                "missing@test.com",
                request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found.");

        verifyNoInteractions(authorizationService);
        verify(interviewRepository, never()).save(interview);
    }

    @Test
    @DisplayName("Authorization failure should leave feedback unchanged")
    void authorizationFailureShouldLeaveFeedbackUnchanged() {

        when(interviewRepository.findById(50L))
                .thenReturn(Optional.of(interview));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));

        doThrow(new ForbiddenException(
                "Recruiter cannot manage applications for another company."))
                .when(authorizationService)
                .checkCanManage(application, recruiter);

        assertThatThrownBy(() -> interviewService.updateFeedback(
                50L,
                "recruiter@test.com",
                request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(
                        "Recruiter cannot manage applications for another company.");

        assertThat(interview.getFeedback()).isNull();
        assertThat(interview.getRating()).isNull();
        verifyNoInteractions(interviewValidator);
        verify(interviewRepository, never()).save(interview);
    }

    @Test
    @DisplayName("Validation failure should leave feedback unchanged")
    void validationFailureShouldLeaveFeedbackUnchanged() {

        when(interviewRepository.findById(50L))
                .thenReturn(Optional.of(interview));
        when(userRepository.findByEmailIgnoreCase(
                "recruiter@test.com"))
                .thenReturn(Optional.of(recruiter));

        doThrow(new BusinessRuleException(
                "Feedback can only be updated for completed interviews."))
                .when(interviewValidator)
                .validateFeedback(interview);

        assertThatThrownBy(() -> interviewService.updateFeedback(
                50L,
                "recruiter@test.com",
                request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Feedback can only be updated for completed interviews.");

        assertThat(interview.getFeedback()).isNull();
        assertThat(interview.getRating()).isNull();
        verify(interviewRepository, never()).save(interview);
        verifyNoInteractions(interviewMapper);
    }
}
