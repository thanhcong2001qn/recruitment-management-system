package com.example.qltd.job.dto.response;

import com.example.qltd.job.enums.EmploymentType;
import com.example.qltd.job.enums.ExperienceLevel;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.enums.WorkingType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class JobResponse {

    private Long id;

    private String title;

    private String slug;

    private String description;

    private String requirements;

    private String responsibilities;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    private String currency;

    private String location;

    private WorkingType workingType;

    private ExperienceLevel experienceLevel;

    private EmploymentType employmentType;

    private JobStatus status;

    private LocalDate deadline;

    private Long companyId;

    private String companyName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}