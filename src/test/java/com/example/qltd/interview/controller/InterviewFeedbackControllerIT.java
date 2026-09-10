package com.example.qltd.interview.controller;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.interview.dto.request.UpdateInterviewFeedbackRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Interview feedback controller integration test")
class InterviewFeedbackControllerIT extends AbstractIntegrationTest {

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
    @DisplayName("Recruiter should update own company interview feedback")
    void recruiterShouldUpdateOwnCompanyInterviewFeedback()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Interview interview = saveInterview(
                companyA,
                InterviewStatus.COMPLETED);

        UpdateInterviewFeedbackRequest request = validRequest();

        mockMvc.perform(
                put(
                        "/api/interviews/{interviewId}/feedback",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Interview feedback updated successfully"))
                .andExpect(jsonPath("$.data.id")
                        .value(interview.getId()))
                .andExpect(jsonPath("$.data.feedback")
                        .value(request.getFeedback()))
                .andExpect(jsonPath("$.data.rating").value(5));

        Interview updated = interviewRepository.findById(
                interview.getId()).orElseThrow();

        assertThat(updated.getFeedback())
                .isEqualTo(request.getFeedback());
        assertThat(updated.getRating()).isEqualTo(5);
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should not update another company feedback")
    void recruiterShouldNotUpdateAnotherCompanyFeedback()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Interview interview = saveInterview(
                companyB,
                InterviewStatus.COMPLETED);

        mockMvc.perform(
                put(
                        "/api/interviews/{interviewId}/feedback",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        Interview unchanged = interviewRepository.findById(
                interview.getId()).orElseThrow();

        assertThat(unchanged.getFeedback()).isNull();
        assertThat(unchanged.getRating()).isNull();
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("Admin should update feedback for any company")
    void adminShouldUpdateFeedbackForAnyCompany()
            throws Exception {

        userRepository.save(
                ApplicationTestFactory.admin("admin@test.com"));
        Interview interview = saveInterview(
                companyB,
                InterviewStatus.COMPLETED);

        mockMvc.perform(
                put(
                        "/api/interviews/{interviewId}/feedback",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
    @DisplayName("Candidate should not update interview feedback")
    void candidateShouldNotUpdateInterviewFeedback()
            throws Exception {

        Interview interview = saveInterview(
                companyA,
                InterviewStatus.COMPLETED);

        mockMvc.perform(
                put(
                        "/api/interviews/{interviewId}/feedback",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Non-completed interview should reject feedback")
    void nonCompletedInterviewShouldRejectFeedback()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Interview interview = saveInterview(
                companyA,
                InterviewStatus.IN_PROGRESS);

        mockMvc.perform(
                put(
                        "/api/interviews/{interviewId}/feedback",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("BUSINESS_RULE_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Feedback can only be updated for completed interviews."));

        Interview unchanged = interviewRepository.findById(
                interview.getId()).orElseThrow();

        assertThat(unchanged.getFeedback()).isNull();
        assertThat(unchanged.getRating()).isNull();
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Invalid feedback request should return validation error")
    void invalidFeedbackRequestShouldReturnValidationError()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Interview interview = saveInterview(
                companyA,
                InterviewStatus.COMPLETED);

        UpdateInterviewFeedbackRequest request = new UpdateInterviewFeedbackRequest();
        request.setFeedback(" ");
        request.setRating(6);

        mockMvc.perform(
                put(
                        "/api/interviews/{interviewId}/feedback",
                        interview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.feedback").exists())
                .andExpect(jsonPath("$.errors.rating").exists());
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Missing interview should return not found")
    void missingInterviewShouldReturnNotFound()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);

        mockMvc.perform(
                put(
                        "/api/interviews/{interviewId}/feedback",
                        999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Interview not found."));
    }

    private UpdateInterviewFeedbackRequest validRequest() {

        UpdateInterviewFeedbackRequest request = new UpdateInterviewFeedbackRequest();
        request.setFeedback(
                "Strong Java fundamentals and clear communication.");
        request.setRating(5);

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
                        "interview-feedback-candidate@test.com"));

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
                LocalDateTime.now().minusDays(1));
        interview.setStatus(status);

        return interviewRepository.save(interview);
    }
}
