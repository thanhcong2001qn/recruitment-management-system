package com.example.qltd.interview.validator;

import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.enums.InterviewStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Interview feedback validator")
class InterviewFeedbackValidatorTest {

    private InterviewValidator interviewValidator;

    @BeforeEach
    void setUp() {

        interviewValidator = new InterviewValidator(
                Clock.systemUTC());
    }

    @Test
    @DisplayName("Completed interview should accept feedback")
    void completedInterviewShouldAcceptFeedback() {

        Interview interview = interviewWithStatus(
                InterviewStatus.COMPLETED);

        assertThatCode(() -> interviewValidator.validateFeedback(
                interview))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Scheduled interview should reject feedback")
    void scheduledInterviewShouldRejectFeedback() {

        Interview interview = interviewWithStatus(
                InterviewStatus.SCHEDULED);

        assertThatThrownBy(() -> interviewValidator.validateFeedback(
                interview))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Feedback can only be updated for completed interviews.");
    }

    @Test
    @DisplayName("In progress interview should reject feedback")
    void inProgressInterviewShouldRejectFeedback() {

        Interview interview = interviewWithStatus(
                InterviewStatus.IN_PROGRESS);

        assertThatThrownBy(() -> interviewValidator.validateFeedback(
                interview))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Feedback can only be updated for completed interviews.");
    }

    @Test
    @DisplayName("Cancelled interview should reject feedback")
    void cancelledInterviewShouldRejectFeedback() {

        Interview interview = interviewWithStatus(
                InterviewStatus.CANCELLED);

        assertThatThrownBy(() -> interviewValidator.validateFeedback(
                interview))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Feedback can only be updated for completed interviews.");
    }

    @Test
    @DisplayName("Null interview should reject feedback")
    void nullInterviewShouldRejectFeedback() {

        assertThatThrownBy(() -> interviewValidator.validateFeedback(
                null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Interview is required.");
    }

    private Interview interviewWithStatus(
            InterviewStatus status) {

        return Interview.builder()
                .status(status)
                .build();
    }
}
