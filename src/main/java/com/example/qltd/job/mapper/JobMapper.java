package com.example.qltd.job.mapper;

import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
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

    public void updateEntity(
            Job job,
            UpdateJobRequest request) {

        if (request.getTitle() != null) {
            job.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            job.setDescription(request.getDescription());
        }

        if (request.getRequirements() != null) {
            job.setRequirements(request.getRequirements());
        }

        if (request.getResponsibilities() != null) {
            job.setResponsibilities(request.getResponsibilities());
        }

        if (request.getSalaryMin() != null) {
            job.setSalaryMin(request.getSalaryMin());
        }

        if (request.getSalaryMax() != null) {
            job.setSalaryMax(request.getSalaryMax());
        }

        if (request.getCurrency() != null) {
            job.setCurrency(
                    request.getCurrency()
                            .trim()
                            .toUpperCase());
        }

        if (request.getLocation() != null) {
            job.setLocation(
                    request.getLocation().trim());
        }

        if (request.getWorkingType() != null) {
            job.setWorkingType(
                    request.getWorkingType());
        }

        if (request.getExperienceLevel() != null) {
            job.setExperienceLevel(
                    request.getExperienceLevel());
        }

        if (request.getEmploymentType() != null) {
            job.setEmploymentType(
                    request.getEmploymentType());
        }

        if (request.getDeadline() != null) {
            job.setDeadline(
                    request.getDeadline());
        }
    }
}