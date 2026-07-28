package com.example.qltd.company.service;

import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.common.exception.DuplicateResourceException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.common.mapper.PageMapper;
import com.example.qltd.company.dto.request.CompanySearchRequest;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.request.UpdateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.enums.CompanyStatus;
import com.example.qltd.company.mapper.CompanyMapper;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.company.service.CompanySlugService;
import com.example.qltd.company.service.impl.CompanyServiceImpl;
import com.example.qltd.company.validator.CompanyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyService")
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private CompanyValidator companyValidator;

    @Mock
    private CompanySlugService companySlugService;

    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company company;
    private CompanyResponse response;
    private CreateCompanyRequest createRequest;
    private UpdateCompanyRequest updateRequest;

    @BeforeEach
    void setUp() {

        createRequest = new CreateCompanyRequest();

        createRequest.setName("OpenAI");

        createRequest.setEmail("contact@openai.com");

        company = Company.builder()
                .id(1L)
                .name("OpenAI")
                .email("contact@openai.com")
                .deleted(false)
                .status(CompanyStatus.ACTIVE)
                .build();

        response = CompanyResponse.builder()
                .id(1L)
                .name("OpenAI")
                .build();

        updateRequest = new UpdateCompanyRequest();

        updateRequest.setName("OpenAI Updated");

    }

    @Nested
    @DisplayName("createCompany()")
    class CreateCompany {

        @Test
        void shouldCreateCompanySuccessfully() {

            when(companyMapper.toEntity(createRequest))
                    .thenReturn(company);

            when(companySlugService.generate("OpenAI"))
                    .thenReturn("openai");

            when(companyRepository.save(company))
                    .thenReturn(company);

            when(companyMapper.toResponse(company))
                    .thenReturn(response);

            CompanyResponse result = companyService.createCompany(createRequest);

            assertThat(result).isNotNull();

            assertThat(result.getId()).isEqualTo(1L);

            assertThat(company.getSlug())
                    .isEqualTo("openai");

            ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);

            verify(companyRepository).save(captor.capture());

            Company saved = captor.getValue();

            assertThat(saved.getSlug())
                    .isEqualTo("openai");

            verify(companyValidator).validate(createRequest);

            verify(companySlugService)
                    .generate("OpenAI");

        }

        @Test
        void shouldThrowDuplicateResourceException() {

            doThrow(new DuplicateResourceException("Company already exists"))
                    .when(companyValidator)
                    .validate(createRequest);

            assertThatThrownBy(() -> companyService.createCompany(createRequest))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(companyRepository, never())
                    .save(any());

        }

        @Test
        void shouldThrowExceptionWhenRepositoryFails() {

            when(companyMapper.toEntity(createRequest))
                    .thenReturn(company);

            when(companySlugService.generate(anyString()))
                    .thenReturn("openai");

            when(companyRepository.save(any()))
                    .thenThrow(new RuntimeException("Database Error"));

            assertThatThrownBy(() -> companyService.createCompany(createRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database Error");

        }

    }

}