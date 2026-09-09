package com.example.qltd.interview.validator;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.interview.dto.request.CreateInterviewRequest;
import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.enums.InterviewStatus;
import com.example.qltd.interview.enums.InterviewType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InterviewValidator {

    private final Clock clock;

    public void validateSchedule(
            Application application,
            CreateInterviewRequest request) {

        if (application.getStatus() != ApplicationStatus.INTERVIEW) {
            throw new BusinessRuleException(
                    "Only applications in INTERVIEW status can be scheduled.");
        }

        if (!request.getScheduledAt()
                .isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleException(
                    "Interview schedule must be in the future.");
        }

        if (request.getInterviewType() == InterviewType.VIDEO
                && isBlank(request.getMeetingUrl())) {
            throw new BusinessRuleException(
                    "Meeting URL is required for video interviews.");
        }

        if (request.getInterviewType() == InterviewType.ONSITE
                && isBlank(request.getLocation())) {
            throw new BusinessRuleException(
                    "Location is required for onsite interviews.");
        }
    }

    public void validateStatusTransition(
            Interview interview,
            InterviewStatus targetStatus) {

        if (interview == null) {
            throw new BusinessRuleException(
                    "Interview is required.");
        }

        if (targetStatus == null) {
            throw new BusinessRuleException(
                    "Target status is required.");
        }

        InterviewStatus currentStatus = interview.getStatus();

        if (currentStatus == null) {
            throw new BusinessRuleException(
                    "Current interview status is required.");
        }

        if (currentStatus == targetStatus) {
            return;
        }

        switch (currentStatus) {

            case SCHEDULED -> validateFromScheduled(
                    targetStatus);

            case IN_PROGRESS -> validateFromInProgress(
                    targetStatus);

            case COMPLETED,
                    CANCELLED,
                    NO_SHOW ->
                throw new BusinessRuleException(
                        "Interview is already in a terminal status.");
        }
    }

    private void validateFromScheduled(
            InterviewStatus targetStatus) {

        if (targetStatus != InterviewStatus.IN_PROGRESS
                && targetStatus != InterviewStatus.CANCELLED
                && targetStatus != InterviewStatus.NO_SHOW) {
            invalidTransition(
                    InterviewStatus.SCHEDULED,
                    targetStatus);
        }
    }

    private void validateFromInProgress(
            InterviewStatus targetStatus) {

        if (targetStatus != InterviewStatus.COMPLETED
                && targetStatus != InterviewStatus.CANCELLED) {
            invalidTransition(
                    InterviewStatus.IN_PROGRESS,
                    targetStatus);
        }
    }

    private void invalidTransition(
            InterviewStatus from,
            InterviewStatus to) {

        throw new BusinessRuleException(
                "Invalid interview status transition: "
                        + from
                        + " -> "
                        + to);
    }

    private boolean isBlank(
            String value) {

        return value == null || value.isBlank();
    }
}
