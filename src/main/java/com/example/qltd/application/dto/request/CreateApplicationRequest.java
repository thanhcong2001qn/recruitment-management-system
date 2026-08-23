package com.example.qltd.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApplicationRequest {

    @Size(max = 5000, message = "Cover letter must not exceed 5000 characters")
    private String coverLetter;

    @Size(max = 500, message = "Resume URL must not exceed 500 characters")
    private String resumeUrl;
}