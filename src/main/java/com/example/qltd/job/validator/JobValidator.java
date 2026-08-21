package com.example.qltd.job.validator;

import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class JobValidator {

    public void validateCreate(
            CreateJobRequest request,
            Company company) {

        validateSalary(
                request.getSalaryMin(),
                request.getSalaryMax());

        validateCompany(company);

        validateDeadline(request.getDeadline());

    }

    public void validateUpdate(
            Job job,
            UpdateJobRequest request) {

        validateUpdatableStatus(job);

        if (request.getSalaryMin() != null
                || request.getSalaryMax() != null) {

            BigDecimal salaryMin = request.getSalaryMin() != null
                    ? request.getSalaryMin()
                    : job.getSalaryMin();

            BigDecimal salaryMax = request.getSalaryMax() != null
                    ? request.getSalaryMax()
                    : job.getSalaryMax();

            validateSalary(
                    salaryMin,
                    salaryMax);
        }

        if (request.getDeadline() != null) {
            validateDeadline(
                    request.getDeadline());
        }
    }

    public void validatePublish(Job job) {

        if (job.getStatus() != JobStatus.DRAFT) {

            throw new BusinessRuleException(
                    "Only DRAFT jobs can be published.");
        }

        if (job.getDeadline() == null) {

            throw new BusinessRuleException(
                    "Job deadline is required before publishing.");
        }

        if (job.getDeadline().isBefore(LocalDate.now())) {

            throw new BusinessRuleException(
                    "Job deadline must not be in the past.");
        }

    }

    private void validateSalary(
            BigDecimal salaryMin,
            BigDecimal salaryMax) {

        if (salaryMin != null
                && salaryMin.compareTo(BigDecimal.ZERO) < 0) {

            throw new BusinessRuleException(
                    "Minimum salary must not be negative.");
        }

        if (salaryMax != null
                && salaryMax.compareTo(BigDecimal.ZERO) < 0) {

            throw new BusinessRuleException(
                    "Maximum salary must not be negative.");
        }

        if (salaryMin != null
                && salaryMax != null
                && salaryMin.compareTo(salaryMax) > 0) {

            throw new BusinessRuleException(
                    "Minimum salary must not be greater than maximum salary.");
        }
    }

    private void validateDeadline(LocalDate deadline) {

        if (deadline.isBefore(LocalDate.now())) {

            throw new BusinessRuleException(
                    "Job deadline must not be in the past.");
        }
    }

    private void validateCompany(Company company) {

        if (company == null) {

            throw new BusinessRuleException(
                    "Company is required.");
        }

        if (Boolean.TRUE.equals(company.getDeleted())) {

            throw new BusinessRuleException(
                    "Cannot create job for a deleted company.");
        }
    }

    private void validateUpdatableStatus(
            Job job) {

        if (job.getStatus() == JobStatus.CLOSED
                || job.getStatus() == JobStatus.EXPIRED
                || job.getStatus() == JobStatus.ARCHIVED) {

            throw new BusinessRuleException(
                    "Job cannot be updated in its current status.");
        }
    }
}