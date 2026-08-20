package com.example.qltd.job.service;

import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;

public interface JobStatusService {

    void transition(Job job, JobStatus targetStatus);

}