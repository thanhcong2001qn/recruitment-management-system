package com.example.qltd.company.service;

import com.example.qltd.common.exception.ForbiddenException;
import com.example.qltd.common.security.CompanyAuthorizationService;
import com.example.qltd.company.entity.Company;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CompanyAuthorizationService")
class CompanyAuthorizationServiceTest {

    private CompanyAuthorizationService authorizationService;

    private Company company;

    @BeforeEach
    void setUp() {
        authorizationService = new CompanyAuthorizationService();
        company = Company.builder().id(1L).build();
    }

    @Test
    @DisplayName("Admin should manage any company")
    void adminShouldManageAnyCompany() {
        User admin = User.builder().role(Role.ADMIN).build();

        assertThatCode(() -> authorizationService.checkCompanyAccess(
                admin,
                99L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Recruiter should manage own company")
    void recruiterShouldManageOwnCompany() {
        User recruiter = recruiterFor(company);

        assertThatCode(() -> authorizationService.checkCompanyAccess(
                recruiter,
                company.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Recruiter should not manage another company")
    void recruiterShouldNotManageAnotherCompany() {
        User recruiter = recruiterFor(company);

        assertThatThrownBy(() -> authorizationService.checkCompanyAccess(
                recruiter,
                2L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Recruiter cannot manage resources of another company.");
    }

    @Test
    @DisplayName("Recruiter without company should be forbidden")
    void recruiterWithoutCompanyShouldBeForbidden() {
        User recruiter = User.builder().role(Role.RECRUITER).build();

        assertThatThrownBy(() -> authorizationService.checkCompanyAccess(
                recruiter,
                1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Recruiter is not assigned to a company.");
    }

    @Test
    @DisplayName("Candidate should be forbidden")
    void candidateShouldBeForbidden() {
        User candidate = User.builder().role(Role.CANDIDATE).build();

        assertThatThrownBy(() -> authorizationService.checkCompanyAccess(
                candidate,
                1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User is not allowed to manage company resources.");
    }

    @Test
    @DisplayName("Missing user should be forbidden")
    void missingUserShouldBeForbidden() {
        assertThatThrownBy(() -> authorizationService.checkCompanyAccess(
                null,
                1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User is required.");
    }

    private User recruiterFor(Company recruiterCompany) {
        return User.builder()
                .role(Role.RECRUITER)
                .company(recruiterCompany)
                .build();
    }
}
