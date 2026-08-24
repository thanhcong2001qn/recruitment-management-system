package com.example.qltd.application.service.impl;

import com.example.qltd.application.dto.request.ApplicationSearchRequest;
import com.example.qltd.application.dto.request.ChangeApplicationStatusRequest;
import com.example.qltd.application.dto.request.CreateApplicationRequest;
import com.example.qltd.application.dto.response.ApplicationResponse;
import com.example.qltd.application.entity.Application;
import com.example.qltd.application.mapper.ApplicationMapper;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.service.ApplicationService;
import com.example.qltd.application.service.ApplicationStatusService;
import com.example.qltd.application.specification.ApplicationSpecification;
import com.example.qltd.application.validator.ApplicationValidator;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.common.exception.DuplicateResourceException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.common.mapper.PageMapper;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationServiceImpl
        implements ApplicationService {

    private final PageMapper pageMapper;

    private final ApplicationRepository applicationRepository;

    private final JobRepository jobRepository;

    private final UserRepository userRepository;

    private final ApplicationMapper applicationMapper;

    private final ApplicationValidator applicationValidator;

    private final ApplicationStatusService applicationStatusService;

    @Override
    public ApplicationResponse createApplication(
            Long jobId,
            String candidateEmail,
            CreateApplicationRequest request) {

        Job job = jobRepository
                .findByIdAndDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found."));

        User candidate = userRepository
                .findByEmailIgnoreCase(
                        candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found."));

        applicationValidator.validateCreate(
                job,
                candidate);

        if (applicationRepository
                .existsByJobIdAndCandidateId(
                        jobId,
                        candidate.getId())) {

            throw new DuplicateResourceException(
                    "Candidate has already applied to this job.");
        }

        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .coverLetter(
                        request.getCoverLetter())
                .resumeUrl(
                        request.getResumeUrl())
                .build();

        Application savedApplication = applicationRepository.save(
                application);

        return applicationMapper.toResponse(
                savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getMyApplication(
            Long applicationId,
            String candidateEmail) {

        User candidate = userRepository
                .findByEmailIgnoreCase(
                        candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found."));

        Application application = applicationRepository
                .findByIdAndCandidateId(
                        applicationId,
                        candidate.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found."));

        return applicationMapper.toResponse(
                application);
    }

    @Override
    public ApplicationResponse changeStatus(
            Long applicationId,
            ChangeApplicationStatusRequest request) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found."));

        applicationStatusService.transition(
                application,
                request.getStatus());

        Application updated = applicationRepository.save(
                application);

        return applicationMapper.toResponse(
                updated);
    }

    @Override
    public ApplicationResponse withdrawApplication(
            Long applicationId,
            String candidateEmail) {

        User candidate = userRepository
                .findByEmailIgnoreCase(
                        candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found."));

        Application application = applicationRepository
                .findByIdAndCandidateId(
                        applicationId,
                        candidate.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found."));

        applicationStatusService.transition(
                application,
                com.example.qltd.application.enums.ApplicationStatus.WITHDRAWN);

        return applicationMapper.toResponse(
                application);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ApplicationResponse> searchApplications(
            Long jobId,
            ApplicationSearchRequest request,
            Pageable pageable) {

        if (!jobRepository.existsByIdAndDeletedFalse(jobId)) {

            throw new ResourceNotFoundException(
                    "Job not found.");
        }

        Specification<Application> specification = ApplicationSpecification.search(
                jobId,
                request);

        Page<Application> applications = applicationRepository.findAll(
                specification,
                pageable);

        return pageMapper.toPagedResponse(
                applications,
                applicationMapper::toResponse);
    }
}