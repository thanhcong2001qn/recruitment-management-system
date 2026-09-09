package com.example.qltd.job.service.impl;

import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.job.service.JobExpirationService;
import com.example.qltd.job.service.JobStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobExpirationServiceImpl
        implements JobExpirationService {

    private final JobRepository jobRepository;

    private final JobStatusService jobStatusService;

    private final Clock clock;

    @Override
    @Transactional
    public int expireJobs() {

        LocalDate today = LocalDate.now(clock);

        List<Job> jobs = jobRepository
                .findAllByStatusAndDeadlineBeforeAndDeletedFalse(
                        JobStatus.PUBLISHED,
                        today);

        for (Job job : jobs) {

            jobStatusService.transition(
                    job,
                    JobStatus.EXPIRED);
        }

        return jobs.size();
    }
}