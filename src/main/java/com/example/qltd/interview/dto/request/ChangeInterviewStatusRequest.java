package com.example.qltd.interview.dto.request;

import com.example.qltd.interview.enums.InterviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeInterviewStatusRequest {

    @NotNull(message = "Interview status is required")
    private InterviewStatus status;
}
