package com.example.qltd.job.service;

import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.response.JobResponse;

public interface JobService {

    JobResponse createJob(CreateJobRequest request);
}