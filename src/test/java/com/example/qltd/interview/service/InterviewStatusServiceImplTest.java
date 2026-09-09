package com.example.qltd.interview.service;

import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.enums.InterviewStatus;
import com.example.qltd.interview.service.impl.InterviewStatusServiceImpl;
import com.example.qltd.interview.validator.InterviewValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewStatusService")
class InterviewStatusServiceImplTest {

    @Mock
    private InterviewValidator interviewValidator;

    @InjectMocks
    private InterviewStatusServiceImpl interviewStatusService;

    private Interview interview;

    @BeforeEach
    void setUp() {

        interview = Interview.builder()
                .status(InterviewStatus.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("Should transition interview successfully")
    void shouldTransitionInterviewSuccessfully() {

        interviewStatusService.transition(
                interview,
                InterviewStatus.IN_PROGRESS);

        assertThat(interview.getStatus())
                .isEqualTo(InterviewStatus.IN_PROGRESS);

        verify(interviewValidator).validateStatusTransition(
                interview,
                InterviewStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Validation failure should leave status unchanged")
    void validationFailureShouldLeaveStatusUnchanged() {

        doThrow(new BusinessRuleException(
                "Invalid interview status transition: SCHEDULED -> COMPLETED"))
                .when(interviewValidator)
                .validateStatusTransition(
                        interview,
                        InterviewStatus.COMPLETED);

        assertThatThrownBy(() -> interviewStatusService.transition(
                interview,
                InterviewStatus.COMPLETED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Invalid interview status transition: SCHEDULED -> COMPLETED");

        assertThat(interview.getStatus())
                .isEqualTo(InterviewStatus.SCHEDULED);
    }
}
