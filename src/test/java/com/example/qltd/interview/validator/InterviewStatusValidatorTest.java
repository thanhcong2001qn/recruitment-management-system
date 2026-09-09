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

@DisplayName("Interview status validator")
class InterviewStatusValidatorTest {

    private InterviewValidator interviewValidator;

    @BeforeEach
    void setUp() {

        interviewValidator = new InterviewValidator(
                Clock.systemUTC());
    }

    @Test
    @DisplayName("SCHEDULED to IN_PROGRESS should be valid")
    void scheduledToInProgressShouldBeValid() {

        Interview interview = interviewWithStatus(
                InterviewStatus.SCHEDULED);

        assertThatCode(() -> interviewValidator
                .validateStatusTransition(
                        interview,
                        InterviewStatus.IN_PROGRESS))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SCHEDULED to CANCELLED should be valid")
    void scheduledToCancelledShouldBeValid() {

        Interview interview = interviewWithStatus(
                InterviewStatus.SCHEDULED);

        assertThatCode(() -> interviewValidator
                .validateStatusTransition(
                        interview,
                        InterviewStatus.CANCELLED))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SCHEDULED to NO_SHOW should be valid")
    void scheduledToNoShowShouldBeValid() {

        Interview interview = interviewWithStatus(
                InterviewStatus.SCHEDULED);

        assertThatCode(() -> interviewValidator
                .validateStatusTransition(
                        interview,
                        InterviewStatus.NO_SHOW))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SCHEDULED to COMPLETED should be rejected")
    void scheduledToCompletedShouldBeRejected() {

        Interview interview = interviewWithStatus(
                InterviewStatus.SCHEDULED);

        assertThatThrownBy(() -> interviewValidator
                .validateStatusTransition(
                        interview,
                        InterviewStatus.COMPLETED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Invalid interview status transition: SCHEDULED -> COMPLETED");
    }

    @Test
    @DisplayName("IN_PROGRESS to COMPLETED should be valid")
    void inProgressToCompletedShouldBeValid() {

        Interview interview = interviewWithStatus(
                InterviewStatus.IN_PROGRESS);

        assertThatCode(() -> interviewValidator
                .validateStatusTransition(
                        interview,
                        InterviewStatus.COMPLETED))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("IN_PROGRESS to NO_SHOW should be rejected")
    void inProgressToNoShowShouldBeRejected() {

        Interview interview = interviewWithStatus(
                InterviewStatus.IN_PROGRESS);

        assertThatThrownBy(() -> interviewValidator
                .validateStatusTransition(
                        interview,
                        InterviewStatus.NO_SHOW))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Invalid interview status transition: IN_PROGRESS -> NO_SHOW");
    }

    @Test
    @DisplayName("Terminal interview should reject another status")
    void terminalInterviewShouldRejectAnotherStatus() {

        Interview interview = interviewWithStatus(
                InterviewStatus.COMPLETED);

        assertThatThrownBy(() -> interviewValidator
                .validateStatusTransition(
                        interview,
                        InterviewStatus.CANCELLED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Interview is already in a terminal status.");
    }

    @Test
    @DisplayName("Same status should be idempotent")
    void sameStatusShouldBeIdempotent() {

        Interview interview = interviewWithStatus(
                InterviewStatus.COMPLETED);

        assertThatCode(() -> interviewValidator
                .validateStatusTransition(
                        interview,
                        InterviewStatus.COMPLETED))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Null interview should be rejected")
    void nullInterviewShouldBeRejected() {

        assertThatThrownBy(() -> interviewValidator
                .validateStatusTransition(
                        null,
                        InterviewStatus.SCHEDULED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Interview is required.");
    }

    @Test
    @DisplayName("Null target status should be rejected")
    void nullTargetStatusShouldBeRejected() {

        Interview interview = interviewWithStatus(
                InterviewStatus.SCHEDULED);

        assertThatThrownBy(() -> interviewValidator
                .validateStatusTransition(
                        interview,
                        null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Target status is required.");
    }

    @Test
    @DisplayName("Null current status should be rejected")
    void nullCurrentStatusShouldBeRejected() {

        Interview interview = interviewWithStatus(null);

        assertThatThrownBy(() -> interviewValidator
                .validateStatusTransition(
                        interview,
                        InterviewStatus.SCHEDULED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Current interview status is required.");
    }

    private Interview interviewWithStatus(
            InterviewStatus status) {

        return Interview.builder()
                .status(status)
                .build();
    }
}
