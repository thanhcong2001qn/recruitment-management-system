package com.example.qltd.job.service;

import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.response.JobResponse;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.mapper.JobMapper;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.job.service.impl.JobServiceImpl;
import com.example.qltd.job.validator.JobValidator;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobService")
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private JobValidator jobValidator;

    @Mock
    private JobSlugService jobSlugService;

    @InjectMocks
    private JobServiceImpl jobService;

    private CreateJobRequest request;

    private Company company;

    private Job job;

    private JobResponse response;

    @BeforeEach
    void setUp() {

        request = new CreateJobRequest();

        request.setTitle("Java Backend Developer");

        request.setCompanyId(1L);

        company = Company.builder()
                .id(1L)
                .name("OpenAI")
                .slug("openai")
                .deleted(false)
                .build();

        job = Job.builder()
                .id(1L)
                .title("Java Backend Developer")
                .slug("java-backend-developer")
                .company(company)
                .build();

        response = JobResponse.builder()
                .id(1L)
                .title("Java Backend Developer")
                .slug("java-backend-developer")
                .companyId(1L)
                .companyName("OpenAI")
                .build();
    }

    @Test
    @DisplayName("Should create job successfully")
    void shouldCreateJobSuccessfully() {

        when(companyRepository
                .findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(company));

        when(jobMapper.toEntity(request))
                .thenReturn(job);

        when(jobSlugService.generate(
                "Java Backend Developer")).thenReturn("java-backend-developer");

        when(jobRepository.save(job))
                .thenReturn(job);

        when(jobMapper.toResponse(job))
                .thenReturn(response);

        JobResponse result = jobService.createJob(request);

        assertThat(result).isNotNull();

        assertThat(result.getId())
                .isEqualTo(1L);

        assertThat(result.getSlug())
                .isEqualTo("java-backend-developer");

        assertThat(job.getCompany())
                .isEqualTo(company);

        verify(companyRepository)
                .findByIdAndDeletedFalse(1L);

        verify(jobValidator)
                .validateCreate(
                        request,
                        company);

        verify(jobSlugService)
                .generate(
                        "Java Backend Developer");

        verify(jobRepository)
                .save(job);
    }

    @Test
    @DisplayName("Should throw when company does not exist")
    void shouldThrowWhenCompanyDoesNotExist() {

        when(companyRepository
                .findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.createJob(request))
                .isInstanceOf(
                        ResourceNotFoundException.class)
                .hasMessage(
                        "Company not found.");

        verify(jobRepository, never())
                .save(any());

        verifyNoInteractions(jobMapper);

        verifyNoInteractions(jobSlugService);

    }

    @Test
    @DisplayName("Should generate slug from normalized title")
    void shouldGenerateSlugFromNormalizedTitle() {

        request.setTitle(
                "   Java Backend Developer   ");

        when(companyRepository
                .findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(company));

        when(jobMapper.toEntity(request))
                .thenReturn(job);

        when(jobSlugService.generate(
                "Java Backend Developer")).thenReturn(
                        "java-backend-developer");

        when(jobRepository.save(job))
                .thenReturn(job);

        when(jobMapper.toResponse(job))
                .thenReturn(response);

        jobService.createJob(request);

        assertThat(job.getTitle())
                .isEqualTo(
                        "Java Backend Developer");

        assertThat(job.getSlug())
                .isEqualTo(
                        "java-backend-developer");

        verify(jobSlugService)
                .generate(
                        "Java Backend Developer");
    }

    @Test
    @DisplayName("Should propagate validation exception")
    void shouldPropagateValidationException() {

        when(companyRepository
                .findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(company));

        doThrow(
                new RuntimeException(
                        "Business validation failed"))
                .when(jobValidator)
                .validateCreate(
                        request,
                        company);

        assertThatThrownBy(() -> jobService.createJob(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(
                        "Business validation failed");

        verify(jobRepository, never())
                .save(any());

        verify(jobSlugService, never())
                .generate(anyString());
    }
}