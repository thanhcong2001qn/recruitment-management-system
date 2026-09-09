package com.example.qltd.job.service.impl;

import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.service.JobStatusService;
import com.example.qltd.job.validator.JobValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobStatusServiceImpl
        implements JobStatusService {

    private final JobValidator jobValidator;

    @Override
    public void transition(
            Job job,
            JobStatus targetStatus) {

        if (job == null) {
            throw new BusinessRuleException(
                    "Job is required.");
        }

        if (targetStatus == null) {
            throw new BusinessRuleException(
                    "Target status is required.");
        }

        JobStatus currentStatus = job.getStatus();

        if (currentStatus == targetStatus) {
            return;
        }

        switch (targetStatus) {

            case PUBLISHED -> publish(job);

            case CLOSED -> close(job);

            case EXPIRED -> expire(job);

            case ARCHIVED -> archive(job);

            case DRAFT ->
                moveToDraft(job);
        }
    }

    private void publish(Job job) {

        if (job.getStatus() != JobStatus.DRAFT) {

            throw new BusinessRuleException(
                    "Only DRAFT jobs can be published.");
        }

        jobValidator.validatePublish(job);

        job.setStatus(
                JobStatus.PUBLISHED);
    }

    private void close(Job job) {

        if (job.getStatus() != JobStatus.PUBLISHED) {

            throw new BusinessRuleException(
                    "Only PUBLISHED jobs can be closed.");
        }

        job.setStatus(
                JobStatus.CLOSED);
    }

    private void expire(Job job) {

        if (job.getStatus() != JobStatus.PUBLISHED) {

            throw new BusinessRuleException(
                    "Only PUBLISHED jobs can expire.");
        }

        job.setStatus(
                JobStatus.EXPIRED);
    }

    private void archive(Job job) {

        if (job.getStatus() != JobStatus.CLOSED
                && job.getStatus() != JobStatus.EXPIRED) {

            throw new BusinessRuleException(
                    "Only CLOSED or EXPIRED jobs can be archived.");
        }

        job.setStatus(
                JobStatus.ARCHIVED);
    }

    private void moveToDraft(Job job) {

        throw new BusinessRuleException(
                "A Job cannot be moved back to DRAFT.");
    }
}