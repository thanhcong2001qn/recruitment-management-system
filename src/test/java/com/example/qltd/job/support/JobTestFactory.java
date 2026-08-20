package com.example.qltd.job.support;

import com.example.qltd.company.entity.Company;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.JobSearchRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.EmploymentType;
import com.example.qltd.job.enums.ExperienceLevel;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.enums.WorkingType;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class JobTestFactory {

    private JobTestFactory() {
    }

    public static Company activeCompany() {

        return Company.builder()
                .id(1L)
                .name("OpenAI")
                .slug("openai")
                .email("contact@openai.com")
                .deleted(false)
                .build();
    }

    public static Company secondActiveCompany() {

        return Company.builder()
                .id(2L)
                .name("Microsoft")
                .slug("microsoft")
                .email("contact@microsoft.com")
                .deleted(false)
                .build();
    }

    public static Company deletedCompany() {

        return Company.builder()
                .id(3L)
                .name("Deleted Company")
                .slug("deleted-company")
                .email("contact@deleted.com")
                .deleted(true)
                .build();
    }

    public static CreateJobRequest createRequest() {

        CreateJobRequest request = new CreateJobRequest();

        request.setTitle("Java Backend Developer");

        request.setDescription(
                "Develop backend applications using Java and Spring Boot.");

        request.setRequirements(
                "Java, Spring Boot, SQL");

        request.setResponsibilities(
                "Design and implement backend APIs.");

        request.setSalaryMin(
                new BigDecimal("15000000"));

        request.setSalaryMax(
                new BigDecimal("30000000"));

        request.setCurrency("VND");

        request.setLocation("Ho Chi Minh");

        request.setWorkingType(
                WorkingType.HYBRID);

        request.setExperienceLevel(
                ExperienceLevel.JUNIOR);

        request.setEmploymentType(
                EmploymentType.FULL_TIME);

        request.setDeadline(
                LocalDate.now().plusDays(30));

        request.setCompanyId(1L);

        return request;
    }

    public static UpdateJobRequest updateRequest() {

        UpdateJobRequest request = new UpdateJobRequest();

        request.setTitle(
                "Senior Java Backend Developer");

        request.setDescription(
                "Updated job description.");

        request.setRequirements(
                "Java, Spring Boot, MySQL, Docker");

        request.setResponsibilities(
                "Lead backend development.");

        request.setSalaryMin(
                new BigDecimal("25000000"));

        request.setSalaryMax(
                new BigDecimal("45000000"));

        request.setCurrency("VND");

        request.setLocation("Ho Chi Minh");

        request.setWorkingType(
                WorkingType.REMOTE);

        request.setExperienceLevel(
                ExperienceLevel.SENIOR);

        request.setEmploymentType(
                EmploymentType.FULL_TIME);

        request.setDeadline(
                LocalDate.now().plusDays(60));

        return request;
    }

    public static Job job(Company company) {

        return Job.builder()
                .id(1L)
                .title("Java Backend Developer")
                .slug("java-backend-developer")
                .description(
                        "Develop backend applications using Java.")
                .requirements(
                        "Java, Spring Boot, SQL")
                .responsibilities(
                        "Build backend APIs.")
                .salaryMin(
                        new BigDecimal("15000000"))
                .salaryMax(
                        new BigDecimal("30000000"))
                .currency("VND")
                .location("Ho Chi Minh")
                .workingType(
                        WorkingType.HYBRID)
                .experienceLevel(
                        ExperienceLevel.JUNIOR)
                .employmentType(
                        EmploymentType.FULL_TIME)
                .status(JobStatus.PUBLISHED)
                .deadline(
                        LocalDate.now().plusDays(30))
                .deleted(false)
                .company(company)
                .build();
    }

    public static Job job(
            Long id,
            String title,
            String slug,
            Company company) {

        Job job = job(company);

        job.setId(id);
        job.setTitle(title);
        job.setSlug(slug);

        return job;
    }

    public static Job draftJob(Company company) {

        Job job = job(company);

        job.setStatus(JobStatus.DRAFT);

        return job;
    }

    public static Job closedJob(Company company) {

        Job job = job(company);

        job.setStatus(JobStatus.CLOSED);

        return job;
    }

    public static Job expiredJob(Company company) {

        Job job = job(company);

        job.setStatus(JobStatus.EXPIRED);

        return job;
    }

    public static Job deletedJob(Company company) {

        Job job = job(company);

        job.setDeleted(true);

        return job;
    }

    public static JobSearchRequest searchRequest() {

        return new JobSearchRequest();
    }

    public static JobSearchRequest keywordSearch(
            String keyword) {

        JobSearchRequest request = new JobSearchRequest();

        request.setKeyword(keyword);

        return request;
    }

    public static JobSearchRequest companySearch(
            Long companyId) {

        JobSearchRequest request = new JobSearchRequest();

        request.setCompanyId(companyId);

        return request;
    }

    public static JobSearchRequest locationSearch(
            String location) {

        JobSearchRequest request = new JobSearchRequest();

        request.setLocation(location);

        return request;
    }

    public static JobSearchRequest statusSearch(
            JobStatus status) {

        JobSearchRequest request = new JobSearchRequest();

        request.setStatus(status);

        return request;
    }

    public static JobSearchRequest workingTypeSearch(
            WorkingType workingType) {

        JobSearchRequest request = new JobSearchRequest();

        request.setWorkingType(workingType);

        return request;
    }

    public static JobSearchRequest experienceLevelSearch(
            ExperienceLevel experienceLevel) {

        JobSearchRequest request = new JobSearchRequest();

        request.setExperienceLevel(
                experienceLevel);

        return request;
    }

    public static JobSearchRequest employmentTypeSearch(
            EmploymentType employmentType) {

        JobSearchRequest request = new JobSearchRequest();

        request.setEmploymentType(
                employmentType);

        return request;
    }
}