package com.example.qltd.interview.controller;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.interview.dto.request.CreateInterviewRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Interview schedule controller integration test")
class InterviewScheduleControllerIT extends AbstractIntegrationTest {

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
    @DisplayName("Recruiter should schedule own company interview")
    void recruiterShouldScheduleOwnCompanyInterview()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Application application = saveApplication(
                companyA,
                ApplicationStatus.INTERVIEW);

        CreateInterviewRequest request = validRequest();

        mockMvc.perform(
                post(
                        "/api/applications/{applicationId}/interviews",
                        application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Interview scheduled successfully"))
                .andExpect(jsonPath("$.data.applicationId")
                        .value(application.getId()))
                .andExpect(jsonPath("$.data.jobId")
                        .value(application.getJob().getId()))
                .andExpect(jsonPath("$.data.candidateEmail")
                        .value("interview-schedule-candidate@test.com"))
                .andExpect(jsonPath("$.data.roundNumber").value(1))
                .andExpect(jsonPath("$.data.interviewType")
                        .value("VIDEO"))
                .andExpect(jsonPath("$.data.status")
                        .value("SCHEDULED"));

        assertThat(interviewRepository
                .findByApplicationIdAndRoundNumber(
                        application.getId(),
                        1))
                .isPresent();
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should not schedule another company interview")
    void recruiterShouldNotScheduleAnotherCompanyInterview()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Application application = saveApplication(
                companyB,
                ApplicationStatus.INTERVIEW);

        mockMvc.perform(
                post(
                        "/api/applications/{applicationId}/interviews",
                        application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        assertThat(interviewRepository.count()).isZero();
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("Admin should schedule interview for any company")
    void adminShouldScheduleInterviewForAnyCompany()
            throws Exception {

        userRepository.save(
                ApplicationTestFactory.admin("admin@test.com"));
        Application application = saveApplication(
                companyB,
                ApplicationStatus.INTERVIEW);

        mockMvc.perform(
                post(
                        "/api/applications/{applicationId}/interviews",
                        application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.applicationId")
                        .value(application.getId()));
    }

    @Test
    @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
    @DisplayName("Candidate should not schedule interview")
    void candidateShouldNotScheduleInterview()
            throws Exception {

        Application application = saveApplication(
                companyA,
                ApplicationStatus.INTERVIEW);

        mockMvc.perform(
                post(
                        "/api/applications/{applicationId}/interviews",
                        application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Application outside interview stage should return bad request")
    void applicationOutsideInterviewStageShouldReturnBadRequest()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Application application = saveApplication(
                companyA,
                ApplicationStatus.SHORTLISTED);

        mockMvc.perform(
                post(
                        "/api/applications/{applicationId}/interviews",
                        application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("BUSINESS_RULE_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Only applications in INTERVIEW status can be scheduled."));
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Duplicate interview round should return conflict")
    void duplicateInterviewRoundShouldReturnConflict()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Application application = saveApplication(
                companyA,
                ApplicationStatus.INTERVIEW);

        Interview existing = InterviewTestFactory.interview(
                application,
                1,
                LocalDateTime.now().plusDays(3));
        interviewRepository.save(existing);

        mockMvc.perform(
                post(
                        "/api/applications/{applicationId}/interviews",
                        application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.message").value(
                        "Interview round already exists for this application."));
    }

    @Test
    @WithMockUser(username = "recruiter-a@test.com", roles = "RECRUITER")
    @DisplayName("Invalid request should return validation error")
    void invalidRequestShouldReturnValidationError()
            throws Exception {

        saveRecruiter("recruiter-a@test.com", companyA);
        Application application = saveApplication(
                companyA,
                ApplicationStatus.INTERVIEW);

        CreateInterviewRequest request = validRequest();
        request.setRoundNumber(0);
        request.setDurationMinutes(10);

        mockMvc.perform(
                post(
                        "/api/applications/{applicationId}/interviews",
                        application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.roundNumber").exists())
                .andExpect(jsonPath("$.errors.durationMinutes").exists());
    }

    private CreateInterviewRequest validRequest() {

        return InterviewTestFactory.createRequest(
                LocalDateTime.now().plusDays(7));
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
            Company company,
            ApplicationStatus status) {

        User candidate = userRepository.save(
                ApplicationTestFactory.candidate(
                        "interview-schedule-candidate@test.com"));

        Job job = jobRepository.save(
                ApplicationTestFactory.publishedJob(company));

        return applicationRepository.save(
                ApplicationTestFactory.application(
                        job,
                        candidate,
                        status));
    }
}
