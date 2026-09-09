package com.example.qltd.interview.dto.request;

import com.example.qltd.interview.enums.InterviewType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateInterviewRequest {

    @NotNull(message = "Round number is required")
    @Min(value = 1, message = "Round number must be at least 1")
    private Integer roundNumber;

    @NotNull(message = "Interview type is required")
    private InterviewType interviewType;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledAt;

    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 480, message = "Duration must not exceed 480 minutes")
    private Integer durationMinutes;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    @Size(max = 500, message = "Meeting URL must not exceed 500 characters")
    private String meetingUrl;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;
}
