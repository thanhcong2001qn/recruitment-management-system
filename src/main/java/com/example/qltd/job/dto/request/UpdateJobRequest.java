package com.example.qltd.job.dto.request;

import com.example.qltd.job.enums.EmploymentType;
import com.example.qltd.job.enums.ExperienceLevel;
import com.example.qltd.job.enums.WorkingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateJobRequest {

    @Size(max = 255, message = "Job title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 5000, message = "Requirements must not exceed 5000 characters")
    private String requirements;

    @Size(max = 5000, message = "Responsibilities must not exceed 5000 characters")
    private String responsibilities;

    @DecimalMin(value = "0.0", message = "Minimum salary must not be negative")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0", message = "Maximum salary must not be negative")
    private BigDecimal salaryMax;

    @Size(max = 10)
    private String currency;

    @Size(max = 255)
    private String location;

    private WorkingType workingType;

    private ExperienceLevel experienceLevel;

    private EmploymentType employmentType;

    private LocalDate deadline;
}