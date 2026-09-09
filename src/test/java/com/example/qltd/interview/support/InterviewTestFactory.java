package com.example.qltd.interview.support;

import com.example.qltd.application.entity.Application;
import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.enums.InterviewStatus;
import com.example.qltd.interview.enums.InterviewType;

import java.time.LocalDateTime;

public final class InterviewTestFactory {

    private InterviewTestFactory() {
    }

    public static Interview interview(
            Application application,
            Integer roundNumber,
            LocalDateTime scheduledAt) {

        return Interview.builder()
                .application(application)
                .roundNumber(roundNumber)
                .interviewType(InterviewType.VIDEO)
                .status(InterviewStatus.SCHEDULED)
                .scheduledAt(scheduledAt)
                .durationMinutes(60)
                .meetingUrl("https://meet.example.com/interview")
                .notes("Technical interview")
                .build();
    }
}
