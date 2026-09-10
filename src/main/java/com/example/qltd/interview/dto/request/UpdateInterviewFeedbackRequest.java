package com.example.qltd.interview.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInterviewFeedbackRequest {

    @NotBlank(message = "Interview feedback is required")
    @Size(max = 5000, message = "Interview feedback must not exceed 5000 characters")
    private String feedback;

    @NotNull(message = "Interview rating is required")
    @Min(value = 1, message = "Interview rating must be at least 1")
    @Max(value = 5, message = "Interview rating must not exceed 5")
    private Integer rating;
}
