package com.example.qltd.job.service.impl;

import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.response.JobResponse;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.mapper.JobMapper;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.job.service.JobService;
import com.example.qltd.job.service.JobSlugService;
import com.example.qltd.job.service.JobStatusService;
import com.example.qltd.job.validator.JobValidator;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.common.mapper.PageMapper;
import com.example.qltd.common.security.CompanyAuthorizationService;
import com.example.qltd.job.dto.request.JobSearchRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
import com.example.qltd.job.specification.JobSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    private final CompanyRepository companyRepository;

    private final JobMapper jobMapper;

    private final JobValidator jobValidator;

    private final JobSlugService jobSlugService;

    private final PageMapper pageMapper;

    private final JobStatusService jobStatusService;

    private final CompanyAuthorizationService companyAuthorizationService;

    private final UserRepository userRepository;

    @Override
    public JobResponse createJob(
            CreateJobRequest request,
            String recruiterEmail) {

        Company company = companyRepository
                .findByIdAndDeletedFalse(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found."));

        User user = userRepository
                .findByEmailIgnoreCase(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        companyAuthorizationService.checkCompanyAccess(
                user,
                company.getId());

        jobValidator.validateCreate(
                request,
                company);

        Job job = jobMapper.toEntity(request);

        String normalizedTitle = request.getTitle().trim();

        job.setTitle(normalizedTitle);

        job.setSlug(
                jobSlugService.generate(
                        normalizedTitle));

        job.setCompany(company);

        Job savedJob = jobRepository.save(job);

        return jobMapper.toResponse(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<JobResponse> searchPublicJobs(
            JobSearchRequest request,
            Pageable pageable) {

        Specification<Job> specification = JobSpecification.searchPublic(
                request);

        Page<Job> jobs = jobRepository.findAll(
                specification,
                pageable);

        return pageMapper.toPagedResponse(
                jobs,
                jobMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<JobResponse> searchManagementJobs(
            JobSearchRequest request,
            Pageable pageable) {

        Specification<Job> specification = JobSpecification.searchManagement(
                request);

        Page<Job> jobs = jobRepository.findAll(
                specification,
                pageable);

        return pageMapper.toPagedResponse(
                jobs,
                jobMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getPublicJobById(
            Long id) {

        Job job = jobRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found."));

        if (job.getStatus() != JobStatus.PUBLISHED) {

            throw new ResourceNotFoundException(
                    "Job not found.");
        }

        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getManagementJobById(
            Long id) {

        Job job = jobRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found."));

        return jobMapper.toResponse(job);
    }

    @Override
    public JobResponse updateJob(
            Long id, String recruiterEmail,
            UpdateJobRequest request) {

        Job job = jobRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found."));
        User user = userRepository
                .findByEmailIgnoreCase(
                        recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        companyAuthorizationService
                .checkCompanyAccess(
                        user,
                        job.getCompany().getId());

        jobValidator.validateUpdate(
                job,
                request);

        String oldTitle = job.getTitle();

        jobMapper.updateEntity(
                job,
                request);

        if (request.getTitle() != null
                && !request.getTitle()
                        .trim()
                        .equals(oldTitle)) {

            String normalizedTitle = request.getTitle().trim();

            job.setSlug(
                    jobSlugService.generate(
                            normalizedTitle,
                            job.getId()));
        }

        Job updatedJob = jobRepository.save(job);

        return jobMapper.toResponse(
                updatedJob);
    }

    @Override
    public void deleteJob(Long id, String recruiterEmail) {

        Job job = jobRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found."));

        User user = userRepository
                .findByEmailIgnoreCase(
                        recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        companyAuthorizationService
                .checkCompanyAccess(
                        user,
                        job.getCompany().getId());

        jobValidator.validateDelete(job);

        job.setDeleted(true);

        jobRepository.save(job);
    }

    @Override
    public JobResponse changeStatus(
            Long id, String recruiterEmail,
            JobStatus targetStatus) {

        Job job = jobRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found."));

        User user = userRepository
                .findByEmailIgnoreCase(
                        recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        companyAuthorizationService
                .checkCompanyAccess(
                        user,
                        job.getCompany().getId());

        jobStatusService.transition(
                job,
                targetStatus);

        Job updatedJob = jobRepository.save(job);

        return jobMapper.toResponse(
                updatedJob);
    }
}
