package com.example.qltd.user.controller;

import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.enums.CompanyStatus;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.shared.enums.UserStatus;
import com.example.qltd.user.dto.request.AssignRecruiterCompanyRequest;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Admin Recruiter Controller Integration Test")
class AdminRecruiterControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company company;

    private User recruiter;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();

        company = companyRepository.save(
                Company.builder()
                        .name("OpenAI")
                        .slug("openai-admin-recruiter-test")
                        .email("admin-recruiter-test@openai.com")
                        .status(CompanyStatus.ACTIVE)
                        .deleted(false)
                        .build());

        recruiter = userRepository.save(
                User.builder()
                        .fullName("Test Recruiter")
                        .email("recruiter-admin-test@example.com")
                        .password("password")
                        .role(Role.RECRUITER)
                        .status(UserStatus.ACTIVE)
                        .company(company)
                        .build());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("Admin should assign recruiter to company")
    void adminShouldAssignRecruiterToCompany()
            throws Exception {
        AssignRecruiterCompanyRequest request = requestFor(company.getId());

        mockMvc.perform(
                patch(
                        "/api/admin/recruiters/{recruiterId}/company",
                        recruiter.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.companyId")
                        .value(company.getId()))
                .andExpect(jsonPath("$.data.companyName")
                        .value("OpenAI"));

        User updated = userRepository.findById(recruiter.getId())
                .orElseThrow();

        assertThat(updated.getCompany().getId())
                .isEqualTo(company.getId());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("Admin should move recruiter to another company")
    void adminShouldMoveRecruiterToAnotherCompany()
            throws Exception {
        Company anotherCompany = companyRepository.save(
                Company.builder()
                        .name("Microsoft")
                        .slug("microsoft-admin-recruiter-test")
                        .email("admin-recruiter-test@microsoft.com")
                        .status(CompanyStatus.ACTIVE)
                        .deleted(false)
                        .build());

        recruiter.setCompany(company);
        userRepository.save(recruiter);

        AssignRecruiterCompanyRequest request = requestFor(
                anotherCompany.getId());

        mockMvc.perform(
                patch(
                        "/api/admin/recruiters/{recruiterId}/company",
                        recruiter.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyId")
                        .value(anotherCompany.getId()));

        User updated = userRepository.findById(recruiter.getId())
                .orElseThrow();

        assertThat(updated.getCompany().getId())
                .isEqualTo(anotherCompany.getId());
    }

    @Test
    @WithMockUser(username = "recruiter@test.com", roles = "RECRUITER")
    @DisplayName("Recruiter should not assign recruiter company")
    void recruiterShouldNotAssignRecruiterCompany()
            throws Exception {
        AssignRecruiterCompanyRequest request = requestFor(company.getId());

        mockMvc.perform(
                patch(
                        "/api/admin/recruiters/{recruiterId}/company",
                        recruiter.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated user should receive 401")
    void unauthenticatedUserShouldReceive401()
            throws Exception {
        AssignRecruiterCompanyRequest request = requestFor(company.getId());

        mockMvc.perform(
                patch(
                        "/api/admin/recruiters/{recruiterId}/company",
                        recruiter.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("Admin should not assign candidate to company")
    void adminShouldNotAssignCandidateToCompany()
            throws Exception {
        User candidate = userRepository.save(
                User.builder()
                        .fullName("Test Candidate")
                        .email("candidate-admin-test@example.com")
                        .password("password")
                        .role(Role.CANDIDATE)
                        .status(UserStatus.ACTIVE)
                        .build());

        AssignRecruiterCompanyRequest request = requestFor(company.getId());

        mockMvc.perform(
                patch(
                        "/api/admin/recruiters/{recruiterId}/company",
                        candidate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("BUSINESS_RULE_ERROR"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("Missing company ID should fail validation")
    void missingCompanyIdShouldFailValidation()
            throws Exception {
        AssignRecruiterCompanyRequest request = new AssignRecruiterCompanyRequest();

        mockMvc.perform(
                patch(
                        "/api/admin/recruiters/{recruiterId}/company",
                        recruiter.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"));
    }

    private AssignRecruiterCompanyRequest requestFor(Long companyId) {
        AssignRecruiterCompanyRequest request = new AssignRecruiterCompanyRequest();
        request.setCompanyId(companyId);
        return request;
    }
}
