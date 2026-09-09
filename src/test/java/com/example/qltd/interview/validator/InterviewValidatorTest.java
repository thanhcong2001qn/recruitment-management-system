package com.example.qltd.interview.validator;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.interview.dto.request.CreateInterviewRequest;
import com.example.qltd.interview.enums.InterviewType;
import com.example.qltd.interview.support.InterviewTestFactory;
import com.example.qltd.job.entity.Job;
import com.example.qltd.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InterviewValidator")
class InterviewValidatorTest {

    private static final ZoneId ZONE_ID = ZoneId.of(
            "Asia/Ho_Chi_Minh");

    private InterviewValidator interviewValidator;

    private Application application;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {

        Clock clock = Clock.fixed(
                Instant.parse("2026-09-09T00:00:00Z"),
                ZONE_ID);

        interviewValidator = new InterviewValidator(clock);
        now = LocalDateTime.now(clock);

        Company company = ApplicationTestFactory.company();
        Job job = ApplicationTestFactory.publishedJob(company);
        User candidate = ApplicationTestFactory.candidate(
                "candidate@test.com");

        application = ApplicationTestFactory.application(
                job,
                candidate,
                ApplicationStatus.INTERVIEW);
    }

    @Test
    @DisplayName("Should allow valid video interview schedule")
    void shouldAllowValidVideoInterviewSchedule() {

        CreateInterviewRequest request = InterviewTestFactory
                .createRequest(now.plusDays(1));

        assertThatCode(() -> interviewValidator.validateSchedule(
                application,
                request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject application outside interview stage")
    void shouldRejectApplicationOutsideInterviewStage() {

        application.setStatus(ApplicationStatus.SHORTLISTED);

        CreateInterviewRequest request = InterviewTestFactory
                .createRequest(now.plusDays(1));

        assertThatThrownBy(() -> interviewValidator.validateSchedule(
                application,
                request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Only applications in INTERVIEW status can be scheduled.");
    }

    @Test
    @DisplayName("Should reject schedule that is not in the future")
    void shouldRejectScheduleThatIsNotInTheFuture() {

        CreateInterviewRequest request = InterviewTestFactory
                .createRequest(now);

        assertThatThrownBy(() -> interviewValidator.validateSchedule(
                application,
                request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Interview schedule must be in the future.");
    }

    @Test
    @DisplayName("Should require meeting URL for video interview")
    void shouldRequireMeetingUrlForVideoInterview() {

        CreateInterviewRequest request = InterviewTestFactory
                .createRequest(now.plusDays(1));
        request.setMeetingUrl(" ");

        assertThatThrownBy(() -> interviewValidator.validateSchedule(
                application,
                request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Meeting URL is required for video interviews.");
    }

    @Test
    @DisplayName("Should require location for onsite interview")
    void shouldRequireLocationForOnsiteInterview() {

        CreateInterviewRequest request = InterviewTestFactory
                .createRequest(now.plusDays(1));
        request.setInterviewType(InterviewType.ONSITE);
        request.setLocation(null);

        assertThatThrownBy(() -> interviewValidator.validateSchedule(
                application,
                request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Location is required for onsite interviews.");
    }
}
