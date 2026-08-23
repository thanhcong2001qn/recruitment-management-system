package com.example.qltd.job.service;

import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.JobSearchRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
import com.example.qltd.job.dto.response.JobResponse;
import com.example.qltd.job.enums.JobStatus;
import org.springframework.data.domain.Pageable;

public interface JobService {

    JobResponse createJob(
            CreateJobRequest request);

    PagedResponse<JobResponse> searchPublicJobs(
            JobSearchRequest request,
            Pageable pageable);

    PagedResponse<JobResponse> searchManagementJobs(
            JobSearchRequest request,
            Pageable pageable);

    JobResponse getPublicJobById(
            Long id);

    JobResponse getManagementJobById(
            Long id);

    JobResponse updateJob(
            Long id,
            UpdateJobRequest request);

    void deleteJob(
            Long id);

    JobResponse changeStatus(
            Long id,
            JobStatus targetStatus);
}