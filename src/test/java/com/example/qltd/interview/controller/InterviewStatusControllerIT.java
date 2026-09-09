package com.example.qltd.interview.controller;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.interview.dto.request.ChangeInterviewStatusRequest;
import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.enums.InterviewStatus;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Interview status controller integration test")
class InterviewStatusControllerIT extends AbstractIntegrationTest {

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
    @DisplayName("Recruiter should start own company interview")
    void recruiterShouldStartOwnCompanyInterview()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Interview interview = saveInterview(
                companyA,
                InterviewStatus.SCHEDULED);

        mockMvc.perform(
                patch(
                        "/api/interviews/{interviewId}/status",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                statusRequest(
                                        InterviewStatus.IN_PROGRESS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Interview status updated successfully"))
                .andExpect(jsonPath("$.data.id")
                        .value(interview.getId()))
                .andExpect(jsonPath("$.data.status")
                        .value("IN_PROGRESS"));

        Interview updated = interviewRepository.findById(
                interview.getId()).orElseThrow();

        assertThat(updated.getStatus())
                .isEqualTo(InterviewStatus.IN_PROGRESS);
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should not update another company interview")
    void recruiterShouldNotUpdateAnotherCompanyInterview()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Interview interview = saveInterview(
                companyB,
                InterviewStatus.SCHEDULED);

        mockMvc.perform(
                patch(
                        "/api/interviews/{interviewId}/status",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                statusRequest(
                                        InterviewStatus.IN_PROGRESS))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        assertThat(interviewRepository.findById(
                interview.getId()).orElseThrow().getStatus())
                .isEqualTo(InterviewStatus.SCHEDULED);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("Admin should update interview for any company")
    void adminShouldUpdateInterviewForAnyCompany()
            throws Exception {

        userRepository.save(
                ApplicationTestFactory.admin("admin@test.com"));
        Interview interview = saveInterview(
                companyB,
                InterviewStatus.SCHEDULED);

        mockMvc.perform(
                patch(
                        "/api/interviews/{interviewId}/status",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                statusRequest(
                                        InterviewStatus.CANCELLED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status")
                        .value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Invalid status transition should return bad request")
    void invalidStatusTransitionShouldReturnBadRequest()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Interview interview = saveInterview(
                companyA,
                InterviewStatus.SCHEDULED);

        mockMvc.perform(
                patch(
                        "/api/interviews/{interviewId}/status",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                statusRequest(
                                        InterviewStatus.COMPLETED))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("BUSINESS_RULE_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Invalid interview status transition: SCHEDULED -> COMPLETED"));

        assertThat(interviewRepository.findById(
                interview.getId()).orElseThrow().getStatus())
                .isEqualTo(InterviewStatus.SCHEDULED);
    }

    @Test
    @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
    @DisplayName("Candidate should not update interview status")
    void candidateShouldNotUpdateInterviewStatus()
            throws Exception {

        Interview interview = saveInterview(
                companyA,
                InterviewStatus.SCHEDULED);

        mockMvc.perform(
                patch(
                        "/api/interviews/{interviewId}/status",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                statusRequest(
                                        InterviewStatus.IN_PROGRESS))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Missing interview should return not found")
    void missingInterviewShouldReturnNotFound()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);

        mockMvc.perform(
                patch(
                        "/api/interviews/{interviewId}/status",
                        999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                statusRequest(
                                        InterviewStatus.IN_PROGRESS))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Interview not found."));
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Missing target status should return validation error")
    void missingTargetStatusShouldReturnValidationError()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Interview interview = saveInterview(
                companyA,
                InterviewStatus.SCHEDULED);

        mockMvc.perform(
                patch(
                        "/api/interviews/{interviewId}/status",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.status").value(
                        "Interview status is required"));
    }

    private ChangeInterviewStatusRequest statusRequest(
            InterviewStatus status) {

        ChangeInterviewStatusRequest request = new ChangeInterviewStatusRequest();
        request.setStatus(status);

        return request;
    }

    private User saveRecruiter(
            String email,
            Company company) {

        return userRepository.save(
                ApplicationTestFactory.recruiter(
                        email,
                        company));
    }

    private Interview saveInterview(
            Company company,
            InterviewStatus status) {

        User candidate = userRepository.save(
                ApplicationTestFactory.candidate(
                        "interview-status-candidate@test.com"));

        Job job = jobRepository.save(
                ApplicationTestFactory.publishedJob(company));

        Application application = applicationRepository.save(
                ApplicationTestFactory.application(
                        job,
                        candidate,
                        ApplicationStatus.INTERVIEW));

        Interview interview = InterviewTestFactory.interview(
                application,
                1,
                LocalDateTime.now().plusDays(3));
        interview.setStatus(status);

        return interviewRepository.save(interview);
    }
}
