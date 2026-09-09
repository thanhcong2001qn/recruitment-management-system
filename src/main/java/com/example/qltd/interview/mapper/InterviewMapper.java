package com.example.qltd.interview.mapper;

import com.example.qltd.application.entity.Application;
import com.example.qltd.interview.dto.response.InterviewResponse;
import com.example.qltd.interview.entity.Interview;
import org.springframework.stereotype.Component;

@Component
public class InterviewMapper {

    public InterviewResponse toResponse(
            Interview interview) {

        Application application = interview.getApplication();

        return InterviewResponse.builder()
                .id(interview.getId())
                .applicationId(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .candidateId(application.getCandidate().getId())
                .candidateName(application.getCandidate().getFullName())
                .candidateEmail(application.getCandidate().getEmail())
                .roundNumber(interview.getRoundNumber())
                .interviewType(interview.getInterviewType())
                .status(interview.getStatus())
                .scheduledAt(interview.getScheduledAt())
                .durationMinutes(interview.getDurationMinutes())
                .location(interview.getLocation())
                .meetingUrl(interview.getMeetingUrl())
                .notes(interview.getNotes())
                .feedback(interview.getFeedback())
                .rating(interview.getRating())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }
}
