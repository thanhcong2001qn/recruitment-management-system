package com.example.qltd.interview.validator;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.interview.dto.request.CreateInterviewRequest;
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

    private boolean isBlank(
            String value) {

        return value == null || value.isBlank();
    }
}
