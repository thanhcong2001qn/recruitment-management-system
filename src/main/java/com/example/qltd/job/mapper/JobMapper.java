package com.example.qltd.job.mapper;

import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.response.JobResponse;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public Job toEntity(CreateJobRequest request) {

        return Job.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .responsibilities(request.getResponsibilities())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .currency(
                        request.getCurrency() == null
                                ? "VND"
                                : request.getCurrency().trim().toUpperCase())
                .location(request.getLocation())
                .workingType(request.getWorkingType())
                .experienceLevel(request.getExperienceLevel())
                .employmentType(request.getEmploymentType())
                .deadline(request.getDeadline())
                .status(JobStatus.DRAFT)
                .deleted(false)
                .build();
    }

    public JobResponse toResponse(Job job) {

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .slug(job.getSlug())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .responsibilities(job.getResponsibilities())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .currency(job.getCurrency())
                .location(job.getLocation())
                .workingType(job.getWorkingType())
                .experienceLevel(job.getExperienceLevel())
                .employmentType(job.getEmploymentType())
                .status(job.getStatus())
                .deadline(job.getDeadline())
                .companyId(job.getCompany().getId())
                .companyName(job.getCompany().getName())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}