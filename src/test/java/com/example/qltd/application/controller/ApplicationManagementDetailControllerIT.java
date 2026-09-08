package com.example.qltd.application.controller;

import com.example.qltd.application.entity.Application;
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

@DisplayName("Application management detail controller integration test")
class ApplicationManagementDetailControllerIT
        extends AbstractIntegrationTest {

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
    @DisplayName("Recruiter should get own company application")
    void recruiterShouldGetOwnCompanyApplication()
            throws Exception {
        saveRecruiter("recruiter-a@test.com", companyA);
        Application application = saveApplication(companyA);

        mockMvc.perform(
                get(
                        "/api/applications/manage/{id}",
                        application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id")
                        .value(application.getId()))
                .andExpect(jsonPath("$.data.jobId")
                        .value(application.getJob().getId()))
                .andExpect(jsonPath("$.data.candidateEmail")
                        .value("candidate-detail@test.com"));
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should not get another company application")
    void recruiterShouldNotGetAnotherCompanyApplication()
            throws Exception {
        saveRecruiter("recruiter-a@test.com", companyA);
        Application application = saveApplication(companyB);

        mockMvc.perform(
                get(
                        "/api/applications/manage/{id}",
                        application.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value(
                        "Recruiter cannot manage applications for another company."));
    }

    @Test
    @WithMockUser(username = "unassigned@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter without company should be forbidden")
    void recruiterWithoutCompanyShouldBeForbidden()
            throws Exception {
        userRepository.save(
                ApplicationTestFactory.recruiter(
                        "unassigned@test.com"));
        Application application = saveApplication(companyA);

        mockMvc.perform(
                get(
                        "/api/applications/manage/{id}",
                        application.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("Admin should get application from any company")
    void adminShouldGetAnyCompanyApplication()
            throws Exception {
        userRepository.save(
                ApplicationTestFactory.admin("admin@test.com"));
        Application application = saveApplication(companyB);

        mockMvc.perform(
                get(
                        "/api/applications/manage/{id}",
                        application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id")
                        .value(application.getId()));
    }

    @Test
    @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
    @DisplayName("Candidate should not access management detail")
    void candidateShouldNotAccessManagementDetail()
            throws Exception {
        Application application = saveApplication(companyA);

        mockMvc.perform(
                get(
                        "/api/applications/manage/{id}",
                        application.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated user should receive 401")
    void unauthenticatedUserShouldReceive401()
            throws Exception {
        Application application = saveApplication(companyA);

        mockMvc.perform(
                get(
                        "/api/applications/manage/{id}",
                        application.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Missing application should return 404")
    void missingApplicationShouldReturn404()
            throws Exception {
        saveRecruiter("recruiter-a@test.com", companyA);

        mockMvc.perform(
                get("/api/applications/manage/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("RESOURCE_NOT_FOUND"));
    }

    private User saveRecruiter(
            String email,
            Company company) {
        return userRepository.save(
                ApplicationTestFactory.recruiter(
                        email,
                        company));
    }

    private Application saveApplication(Company company) {
        User candidate = userRepository.save(
                ApplicationTestFactory.candidate(
                        "candidate-detail@test.com"));
        Job job = jobRepository.save(
                ApplicationTestFactory.publishedJob(company));
        return applicationRepository.save(
                ApplicationTestFactory.application(
                        job,
                        candidate));
    }
}
