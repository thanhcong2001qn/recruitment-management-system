package com.example.qltd.job.service;

import org.springframework.data.domain.Pageable;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.JobSearchRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
import com.example.qltd.job.dto.response.JobResponse;

public interface JobService {

    JobResponse createJob(CreateJobRequest request);

    PagedResponse<JobResponse> searchJobs(
            JobSearchRequest request,
            Pageable pageable,
            boolean publicSearch);

    JobResponse getJobById(
            Long id,
            boolean publicSearch);

    JobResponse updateJob(
            Long id,
            UpdateJobRequest request);

    void deleteJob(Long id);
}