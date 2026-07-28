package com.example.qltd.company.service;

import com.example.qltd.common.mapper.PageMapper;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.mapper.CompanyMapper;
import com.example.qltd.company.repository.CompanyRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    private CreateCompanyRequest request;
    private Company company;
    private Company savedCompany;
    private CompanyResponse response;

    @BeforeEach
    void setUp() {

        request = new CreateCompanyRequest();
        request.setName("OpenAI");
        request.setEmail("contact@openai.com");

        company = Company.builder()
                .name("OpenAI")
                .email("contact@openai.com")
                .build();

        savedCompany = Company.builder()
                .id(1L)
                .name("OpenAI")
                .slug("openai")
                .email("contact@openai.com")
                .build();

        response = CompanyResponse.builder()
                .id(1L)
                .name("OpenAI")
                .slug("openai")
                .email("contact@openai.com")
                .build();

    }

    @Nested
    @DisplayName("createCompany()")
    class CreateCompany {

        @Test
        @DisplayName("Should create company successfully")
        void shouldCreateCompanySuccessfully() {

            // Arrange

            doNothing().when(companyValidator).validate(request);

            when(companyMapper.toEntity(request))
                    .thenReturn(company);

            when(companySlugService.generate("OpenAI"))
                    .thenReturn("openai");

            when(companyRepository.save(company))
                    .thenReturn(savedCompany);

            when(companyMapper.toResponse(savedCompany))
                    .thenReturn(response);

            // Act

            CompanyResponse result = companyService.createCompany(request);

            // Assert

            assertThat(result).isNotNull();

            assertThat(result.getId()).isEqualTo(1L);

            assertThat(result.getName()).isEqualTo("OpenAI");

            assertThat(result.getSlug()).isEqualTo("openai");

            assertThat(company.getSlug()).isEqualTo("openai");

            // Verify order

            verify(companyValidator).validate(request);

            verify(companyMapper).toEntity(request);

            verify(companySlugService).generate("OpenAI");

            ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);

            verify(companyRepository).save(captor.capture());

            Company actual = captor.getValue();

            assertThat(actual.getSlug()).isEqualTo("openai");

            assertThat(actual.getName()).isEqualTo("OpenAI");

            assertThat(actual.getEmail()).isEqualTo("contact@openai.com");

            verify(companyMapper).toResponse(savedCompany);

            verifyNoMoreInteractions(
                    companyRepository,
                    companyMapper,
                    companyValidator,
                    companySlugService);

        }

    }

}