package com.example.qltd.interview.dto.response;

import com.example.qltd.interview.enums.InterviewStatus;
import com.example.qltd.interview.enums.InterviewType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewResponse {

    private Long id;

    private Long applicationId;

    private Long jobId;

    private String jobTitle;

    private Long candidateId;

    private String candidateName;

    private String candidateEmail;

    private Integer roundNumber;

    private InterviewType interviewType;

    private InterviewStatus status;

    private LocalDateTime scheduledAt;

    private Integer durationMinutes;

    private String location;

    private String meetingUrl;

    private String notes;

    private String feedback;

    private Integer rating;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
