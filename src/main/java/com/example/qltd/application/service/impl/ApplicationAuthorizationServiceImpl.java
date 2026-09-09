package com.example.qltd.application.service.impl;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.service.ApplicationAuthorizationService;
import com.example.qltd.common.exception.ForbiddenException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationAuthorizationServiceImpl
        implements ApplicationAuthorizationService {

    private final JobRepository jobRepository;

    @Override
    public void checkCanManage(
            Application application,
            User user) {

        if (application == null) {
            throw new ResourceNotFoundException(
                    "Application not found.");
        }

        if (user == null) {
            throw new ForbiddenException(
                    "User is required.");
        }

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (user.getRole() != Role.RECRUITER) {
            throw new ForbiddenException(
                    "User is not allowed to manage applications.");
        }

        if (user.getCompany() == null) {
            throw new ForbiddenException(
                    "Recruiter is not assigned to a company.");
        }

        Long recruiterCompanyId = user.getCompany().getId();

        Long jobCompanyId = application.getJob()
                .getCompany()
                .getId();

        if (!recruiterCompanyId.equals(
                jobCompanyId)) {

            throw new ForbiddenException(
                    "Recruiter cannot manage applications for another company.");
        }
    }

    @Override
    public void checkCanManageJob(
            Long jobId,
            User user) {

        Job job = jobRepository
                .findByIdAndDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found."));

        if (user == null) {
            throw new ForbiddenException(
                    "User is required.");
        }

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (user.getRole() != Role.RECRUITER) {
            throw new ForbiddenException(
                    "User is not allowed to manage applications.");
        }

        if (user.getCompany() == null) {
            throw new ForbiddenException(
                    "Recruiter is not assigned to a company.");
        }

        if (!user.getCompany()
                .getId()
                .equals(
                        job.getCompany().getId())) {

            throw new ForbiddenException(
                    "Recruiter cannot manage applications for another company.");
        }
    }
}