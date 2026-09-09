package com.example.qltd.user.service;

import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.enums.CompanyStatus;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.user.dto.respone.RecruiterResponse;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import com.example.qltd.user.service.impl.RecruiterManagementServiceImpl;
import com.example.qltd.user.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecruiterManagementService")
class RecruiterManagementServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private RecruiterManagementServiceImpl recruiterManagementService;

    private User recruiter;

    private Company company;

    @BeforeEach
    void setUp() {
        recruiter = User.builder()
                .id(1L)
                .fullName("Test Recruiter")
                .email("recruiter@test.com")
                .role(Role.RECRUITER)
                .company(company)
                .build();

        company = Company.builder()
                .id(10L)
                .name("OpenAI")
                .status(CompanyStatus.ACTIVE)
                .deleted(false)
                .build();
    }

    @Test
    @DisplayName("Admin flow should assign recruiter to active company")
    void shouldAssignRecruiterToActiveCompany() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(recruiter));
        when(companyRepository.findByIdAndDeletedFalse(10L))
                .thenReturn(Optional.of(company));
        when(userRepository.save(recruiter))
                .thenReturn(recruiter);

        RecruiterResponse result = recruiterManagementService.assignCompany(
                1L,
                10L);

        assertThat(recruiter.getCompany()).isSameAs(company);
        assertThat(result.getCompanyId()).isEqualTo(10L);
        assertThat(result.getCompanyName()).isEqualTo("OpenAI");

        verify(userValidator).validateRecruiterCompany(recruiter);
        verify(userRepository).save(recruiter);
    }

    @Test
    @DisplayName("Should return not found when recruiter does not exist")
    void shouldRejectMissingRecruiter() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> recruiterManagementService.assignCompany(
                999L,
                10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found.");

        verifyNoInteractions(companyRepository);
    }

    @Test
    @DisplayName("Should reject user that is not recruiter")
    void shouldRejectNonRecruiter() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(recruiter));
        doThrow(new BusinessRuleException("User is not a recruiter."))
                .when(userValidator)
                .validateRecruiterCompany(recruiter);

        assertThatThrownBy(() -> recruiterManagementService.assignCompany(
                1L,
                10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User is not a recruiter.");

        verifyNoInteractions(companyRepository);
        verify(userRepository, never()).save(recruiter);
    }

    @Test
    @DisplayName("Should return not found when company does not exist")
    void shouldRejectMissingCompany() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(recruiter));
        when(companyRepository.findByIdAndDeletedFalse(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> recruiterManagementService.assignCompany(
                1L,
                999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Company not found.");

        verify(userRepository, never()).save(recruiter);
    }

    @Test
    @DisplayName("Should reject inactive company")
    void shouldRejectInactiveCompany() {
        company.setStatus(CompanyStatus.INACTIVE);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(recruiter));
        when(companyRepository.findByIdAndDeletedFalse(10L))
                .thenReturn(Optional.of(company));

        assertThatThrownBy(() -> recruiterManagementService.assignCompany(
                1L,
                10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Recruiter can only be assigned to an active company.");

        verify(userRepository, never()).save(recruiter);
    }
}
