package com.example.qltd.application.controller;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Application candidate search controller integration test")
class ApplicationCandidateSearchControllerIT
        extends AbstractIntegrationTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company company;

    private Job job;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();

        company = companyRepository.save(
                ApplicationTestFactory.company());

        userRepository.save(
                ApplicationTestFactory.recruiter(
                        "recruiter-search@test.com",
                        company));

        job = jobRepository.save(
                ApplicationTestFactory.publishedJob(company));

        saveApplication(
                "Nguyen Van An",
                "an.candidate@example.com",
                ApplicationStatus.APPLIED);
        saveApplication(
                "Tran Thi Binh",
                "binh.candidate@example.com",
                ApplicationStatus.SCREENING);
    }

    @Test
    @WithMockUser(username = "recruiter-search@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should search applications by candidate name")
    void recruiterShouldSearchByCandidateName()
            throws Exception {
        mockMvc.perform(
                get(
                        "/api/jobs/{jobId}/applications",
                        job.getId())
                        .param("keyword", "van an"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].candidateName")
                        .value("Nguyen Van An"));
    }

    @Test
    @WithMockUser(username = "recruiter-search@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should search applications by candidate email")
    void recruiterShouldSearchByCandidateEmail()
            throws Exception {
        mockMvc.perform(
                get(
                        "/api/jobs/{jobId}/applications",
                        job.getId())
                        .param("keyword", "BINH.CANDIDATE@EXAMPLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].candidateEmail")
                        .value("binh.candidate@example.com"));
    }

    @Test
    @WithMockUser(username = "recruiter-search@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should combine keyword and status filters")
    void recruiterShouldCombineKeywordAndStatus()
            throws Exception {
        mockMvc.perform(
                get(
                        "/api/jobs/{jobId}/applications",
                        job.getId())
                        .param("keyword", "candidate@example.com")
                        .param("status", "SCREENING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].candidateName")
                        .value("Tran Thi Binh"))
                .andExpect(jsonPath("$.data.items[0].status")
                        .value("SCREENING"));
    }

    @Test
    @WithMockUser(username = "recruiter-search@test.com", roles = "RECRUITER")
    @DisplayName("Blank keyword should not filter applications")
    void blankKeywordShouldNotFilterApplications()
            throws Exception {
        mockMvc.perform(
                get(
                        "/api/jobs/{jobId}/applications",
                        job.getId())
                        .param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    private Application saveApplication(
            String fullName,
            String email,
            ApplicationStatus status) {
        User candidate = ApplicationTestFactory.candidate(email);
        candidate.setFullName(fullName);
        candidate = userRepository.save(candidate);

        return applicationRepository.save(
                ApplicationTestFactory.application(
                        job,
                        candidate,
                        status));
    }
}
