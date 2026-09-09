package com.example.qltd.application.dto.request;

import com.example.qltd.application.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeApplicationStatusRequest {

    @NotNull(message = "Application status is required")
    private ApplicationStatus status;
}