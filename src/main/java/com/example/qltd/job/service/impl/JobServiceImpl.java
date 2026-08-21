package com.example.qltd.job.service.impl;

import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.response.JobResponse;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.mapper.JobMapper;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.job.service.JobService;
import com.example.qltd.job.service.JobSlugService;
import com.example.qltd.job.validator.JobValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.common.mapper.PageMapper;
import com.example.qltd.job.dto.request.JobSearchRequest;
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

    @Override
    public JobResponse createJob(CreateJobRequest request) {

        Company company = companyRepository
                .findByIdAndDeletedFalse(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found."));

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
    public PagedResponse<JobResponse> searchJobs(
            JobSearchRequest request,
            Pageable pageable,
            boolean publicSearch) {

        Specification<Job> specification = JobSpecification.search(
                request,
                publicSearch);

        Page<Job> jobs = jobRepository.findAll(
                specification,
                pageable);

        return pageMapper.toPagedResponse(
                jobs,
                jobMapper::toResponse);
    }
}