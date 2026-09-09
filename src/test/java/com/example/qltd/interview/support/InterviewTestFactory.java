package com.example.qltd.interview.support;

import com.example.qltd.application.entity.Application;
import com.example.qltd.interview.dto.request.CreateInterviewRequest;
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

    public static CreateInterviewRequest createRequest(
            LocalDateTime scheduledAt) {

        CreateInterviewRequest request = new CreateInterviewRequest();

        request.setRoundNumber(1);
        request.setInterviewType(InterviewType.VIDEO);
        request.setScheduledAt(scheduledAt);
        request.setDurationMinutes(60);
        request.setMeetingUrl(
                "https://meet.example.com/interview");
        request.setNotes("Technical interview");

        return request;
    }
}