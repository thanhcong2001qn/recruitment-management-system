package com.example.qltd.interview.controller;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.repository.InterviewRepository;
import com.example.qltd.interview.support.InterviewTestFactory;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Interview list controller integration test")
class InterviewListControllerIT extends AbstractIntegrationTest {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company companyA;

    private Company companyB;

    @BeforeEach
    void setUp() {

        interviewRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();

        companyA = companyRepository.save(
                ApplicationTestFactory.company());
        companyB = companyRepository.save(
                ApplicationTestFactory.company());
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should get own company interviews in round order")
    void recruiterShouldGetOwnCompanyInterviewsInRoundOrder()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Application application = saveApplication(companyA);

        saveInterview(application, 2);
        saveInterview(application, 1);

        mockMvc.perform(
                get(
                        "/api/applications/{applicationId}/interviews",
                        application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Application interviews retrieved successfully"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].roundNumber").value(1))
                .andExpect(jsonPath("$.data[1].roundNumber").value(2))
                .andExpect(jsonPath("$.data[0].applicationId")
                        .value(application.getId()))
                .andExpect(jsonPath("$.data[0].candidateEmail")
                        .value("interview-list-candidate@test.com"));
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should not get another company interviews")
    void recruiterShouldNotGetAnotherCompanyInterviews()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Application application = saveApplication(companyB);
        saveInterview(application, 1);

        mockMvc.perform(
                get(
                        "/api/applications/{applicationId}/interviews",
                        application.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("Admin should get interviews for any company")
    void adminShouldGetInterviewsForAnyCompany()
            throws Exception {

        userRepository.save(
                ApplicationTestFactory.admin("admin@test.com"));
        Application application = saveApplication(companyB);
        saveInterview(application, 1);

        mockMvc.perform(
                get(
                        "/api/applications/{applicationId}/interviews",
                        application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].roundNumber").value(1));
    }

    @Test
    @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
    @DisplayName("Candidate should not get internal interview list")
    void candidateShouldNotGetInternalInterviewList()
            throws Exception {

        Application application = saveApplication(companyA);

        mockMvc.perform(
                get(
                        "/api/applications/{applicationId}/interviews",
                        application.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Missing application should return not found")
    void missingApplicationShouldReturnNotFound()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);

        mockMvc.perform(
                get(
                        "/api/applications/{applicationId}/interviews",
                        999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Application not found."));
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Application without interviews should return empty list")
    void applicationWithoutInterviewsShouldReturnEmptyList()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Application application = saveApplication(companyA);

        mockMvc.perform(
                get(
                        "/api/applications/{applicationId}/interviews",
                        application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        assertThat(interviewRepository.count()).isZero();
    }

    private User saveRecruiter(
            String email,
            Company company) {

        return userRepository.save(
                ApplicationTestFactory.recruiter(
                        email,
                        company));
    }

    private Application saveApplication(
            Company company) {

        User candidate = userRepository.save(
                ApplicationTestFactory.candidate(
                        "interview-list-candidate@test.com"));

        Job job = jobRepository.save(
                ApplicationTestFactory.publishedJob(company));

        return applicationRepository.save(
                ApplicationTestFactory.application(
                        job,
                        candidate,
                        ApplicationStatus.INTERVIEW));
    }

    private Interview saveInterview(
            Application application,
            int roundNumber) {

        return interviewRepository.save(
                InterviewTestFactory.interview(
                        application,
                        roundNumber,
                        LocalDateTime.now().plusDays(roundNumber)));
    }
}
