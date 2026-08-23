package com.example.qltd.application.support;

import com.example.qltd.application.dto.request.ChangeApplicationStatusRequest;
import com.example.qltd.application.dto.request.CreateApplicationRequest;
import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.company.entity.Company;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.EmploymentType;
import com.example.qltd.job.enums.ExperienceLevel;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.enums.WorkingType;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.shared.enums.UserStatus;
import com.example.qltd.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ApplicationTestFactory {

    private ApplicationTestFactory() {
    }

    public static Company company() {

        return Company.builder()
                .name("OpenAI")
                .slug(
                        "openai-" +
                                System.nanoTime())
                .email("company@test.com")
                .deleted(false)
                .build();
    }

    public static User candidate(
            String email) {

        return User.builder()
                .fullName("Test Candidate")
                .email(email)
                .password("password")
                .phone("0900000000")
                .role(Role.CANDIDATE)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public static User recruiter(
            String email) {

        return User.builder()
                .fullName("Test Recruiter")
                .email(email)
                .password("password")
                .phone("0900000001")
                .role(Role.RECRUITER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public static User admin(
            String email) {

        return User.builder()
                .fullName("Test Admin")
                .email(email)
                .password("password")
                .phone("0900000002")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public static User inactiveCandidate(
            String email) {

        return User.builder()
                .fullName("Inactive Candidate")
                .email(email)
                .password("password")
                .phone("0900000003")
                .role(Role.CANDIDATE)
                .status(UserStatus.INACTIVE)
                .build();
    }

    public static User recruiterAsCandidate(
            String email) {

        return User.builder()
                .fullName("Recruiter User")
                .email(email)
                .password("password")
                .phone("0900000004")
                .role(Role.RECRUITER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public static Job publishedJob(
            Company company) {

        return Job.builder()
                .title("Java Backend Developer")
                .slug(
                        "java-backend-developer-" +
                                System.nanoTime())
                .description(
                        "Build backend applications.")
                .requirements(
                        "Java, Spring Boot, SQL")
                .responsibilities(
                        "Develop REST APIs.")
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
                .status(
                        JobStatus.PUBLISHED)
                .deadline(
                        LocalDate.now().plusDays(30))
                .deleted(false)
                .company(company)
                .build();
    }

    public static Job draftJob(
            Company company) {

        Job job = publishedJob(company);

        job.setTitle(
                "Draft Java Developer");

        job.setSlug(
                "draft-java-developer-" +
                        System.nanoTime());

        job.setStatus(
                JobStatus.DRAFT);

        return job;
    }

    public static Job closedJob(
            Company company) {

        Job job = publishedJob(company);

        job.setTitle(
                "Closed Java Developer");

        job.setSlug(
                "closed-java-developer-" +
                        System.nanoTime());

        job.setStatus(
                JobStatus.CLOSED);

        return job;
    }

    public static CreateApplicationRequest createRequest() {

        CreateApplicationRequest request = new CreateApplicationRequest();

        request.setCoverLetter(
                "I am very interested in this position.");

        request.setResumeUrl(
                "https://example.com/resume.pdf");

        return request;
    }

    public static ChangeApplicationStatusRequest statusRequest(
            ApplicationStatus status) {

        ChangeApplicationStatusRequest request = new ChangeApplicationStatusRequest();

        request.setStatus(status);

        return request;
    }

    public static Application application(
            Job job,
            User candidate) {

        return Application.builder()
                .job(job)
                .candidate(candidate)
                .coverLetter(
                        "I am interested in this job.")
                .resumeUrl(
                        "https://example.com/resume.pdf")
                .status(
                        ApplicationStatus.APPLIED)
                .build();
    }

    public static Application application(
            Job job,
            User candidate,
            ApplicationStatus status) {

        Application application = application(
                job,
                candidate);

        application.setStatus(status);

        return application;
    }
}