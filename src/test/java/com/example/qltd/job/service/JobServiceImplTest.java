package com.example.qltd.job.service;

import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.common.mapper.PageMapper;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.JobSearchRequest;
import com.example.qltd.job.dto.response.JobResponse;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.mapper.JobMapper;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.job.service.impl.JobServiceImpl;
import com.example.qltd.job.support.JobTestFactory;
import com.example.qltd.job.validator.JobValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    private CreateJobRequest createRequest;

    private Company company;

    private Job job;

    private JobResponse response;

    @BeforeEach
    void setUp() {

        createRequest = JobTestFactory.createRequest();

        company = JobTestFactory.activeCompany();

        job = JobTestFactory.job(company);

        response = JobResponse.builder()
                .id(1L)
                .title("Java Backend Developer")
                .slug("java-backend-developer")
                .companyId(1L)
                .companyName("OpenAI")
                .build();
    }

    @Nested
    @DisplayName("createJob()")
    class CreateJobTest {

        @Test
        @DisplayName("Should create job successfully")
        void shouldCreateJobSuccessfully() {

            when(
                    companyRepository
                            .findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(company));

            when(
                    jobMapper.toEntity(createRequest))
                    .thenReturn(job);

            when(
                    jobSlugService.generate(
                            "Java Backend Developer"))
                    .thenReturn(
                            "java-backend-developer");

            when(
                    jobRepository.save(job))
                    .thenReturn(job);

            when(
                    jobMapper.toResponse(job))
                    .thenReturn(response);

            JobResponse result = jobService.createJob(
                    createRequest);

            assertThat(result)
                    .isNotNull();

            assertThat(result.getId())
                    .isEqualTo(1L);

            assertThat(result.getTitle())
                    .isEqualTo(
                            "Java Backend Developer");

            assertThat(result.getSlug())
                    .isEqualTo(
                            "java-backend-developer");

            assertThat(job.getCompany())
                    .isEqualTo(company);

            verify(companyRepository)
                    .findByIdAndDeletedFalse(1L);

            verify(jobValidator)
                    .validateCreate(
                            createRequest,
                            company);

            verify(jobSlugService)
                    .generate(
                            "Java Backend Developer");

            verify(jobRepository)
                    .save(job);

            verify(jobMapper)
                    .toResponse(job);
        }

        @Test
        @DisplayName("Should trim title before generating slug")
        void shouldNormalizeTitleBeforeGeneratingSlug() {

            createRequest.setTitle(
                    "   Java Backend Developer   ");

            when(
                    companyRepository
                            .findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(company));

            when(
                    jobMapper.toEntity(createRequest))
                    .thenReturn(job);

            when(
                    jobSlugService.generate(
                            "Java Backend Developer"))
                    .thenReturn(
                            "java-backend-developer");

            when(
                    jobRepository.save(job))
                    .thenReturn(job);

            when(
                    jobMapper.toResponse(job))
                    .thenReturn(response);

            jobService.createJob(
                    createRequest);

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
        @DisplayName("Should throw when company does not exist")
        void shouldThrowWhenCompanyDoesNotExist() {

            when(
                    companyRepository
                            .findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> jobService.createJob(
                    createRequest))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Company not found.");

            verify(jobRepository, never())
                    .save(any());

            verifyNoInteractions(jobMapper);

            verifyNoInteractions(
                    jobSlugService);

            verifyNoInteractions(
                    jobValidator);
        }

        @Test
        @DisplayName("Should stop when business validation fails")
        void shouldStopWhenValidationFails() {

            when(
                    companyRepository
                            .findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(company));

            doThrow(
                    new RuntimeException(
                            "Business validation failed"))
                    .when(jobValidator)
                    .validateCreate(
                            createRequest,
                            company);

            assertThatThrownBy(() -> jobService.createJob(
                    createRequest))
                    .isInstanceOf(
                            RuntimeException.class)
                    .hasMessage(
                            "Business validation failed");

            verify(jobRepository, never())
                    .save(any());

            verifyNoInteractions(
                    jobSlugService);
        }
    }

    @Nested
    @DisplayName("searchJobs()")
    class SearchJobsTest {

        @Test
        @DisplayName("Should return paged jobs")
        void shouldReturnPagedJobs() {

            JobSearchRequest request = JobTestFactory.searchRequest();

            Pageable pageable = PageRequest.of(
                    0,
                    10);

            Page<Job> page = new PageImpl<>(
                    List.of(job),
                    pageable,
                    1);

            @SuppressWarnings("unchecked")
            PagedResponse<JobResponse> expected = mock(PagedResponse.class);

            when(
                    jobRepository.findAll(
                            any(Specification.class),
                            eq(pageable)))
                    .thenReturn(page);

            when(
                    pageMapper.<Job, JobResponse>toPagedResponse(
                            eq(page),
                            any()))
                    .thenReturn(expected);

            PagedResponse<JobResponse> result = jobService.searchJobs(
                    request,
                    pageable);

            assertThat(result)
                    .isSameAs(expected);

            verify(jobRepository)
                    .findAll(
                            any(Specification.class),
                            eq(pageable));

            verify(pageMapper)
                    .toPagedResponse(
                            eq(page),
                            any());
        }

        @Test
        @DisplayName("Should return empty paged response")
        void shouldReturnEmptyPagedResponse() {

            JobSearchRequest request = JobTestFactory.searchRequest();

            Pageable pageable = PageRequest.of(
                    0,
                    10);

            Page<Job> emptyPage = Page.empty(pageable);

            @SuppressWarnings("unchecked")
            PagedResponse<JobResponse> expected = mock(PagedResponse.class);

            when(
                    jobRepository.findAll(
                            any(Specification.class),
                            eq(pageable)))
                    .thenReturn(emptyPage);

            when(
                    pageMapper.<Job, JobResponse>toPagedResponse(
                            eq(emptyPage),
                            any()))
                    .thenReturn(expected);

            PagedResponse<JobResponse> result = jobService.searchJobs(
                    request,
                    pageable);

            assertThat(result)
                    .isSameAs(expected);

            verify(jobRepository)
                    .findAll(
                            any(Specification.class),
                            eq(pageable));

            verify(pageMapper)
                    .toPagedResponse(
                            eq(emptyPage),
                            any());
        }

        @Test
        @DisplayName("Should pass specification and pageable to repository")
        void shouldPassSpecificationAndPageable() {

            JobSearchRequest request = JobTestFactory.keywordSearch(
                    "Java");

            Pageable pageable = PageRequest.of(
                    1,
                    5);

            Page<Job> page = new PageImpl<>(
                    List.of(job),
                    pageable,
                    6);

            @SuppressWarnings("unchecked")
            PagedResponse<JobResponse> expected = mock(PagedResponse.class);

            when(
                    jobRepository.findAll(
                            any(Specification.class),
                            eq(pageable)))
                    .thenReturn(page);

            when(
                    pageMapper.<Job, JobResponse>toPagedResponse(
                            eq(page),
                            any()))
                    .thenReturn(expected);

            jobService.searchJobs(
                    request,
                    pageable);

            ArgumentCaptor<Specification<Job>> specificationCaptor = ArgumentCaptor.forClass(
                    Specification.class);

            verify(jobRepository)
                    .findAll(
                            specificationCaptor.capture(),
                            eq(pageable));

            assertThat(
                    specificationCaptor.getValue())
                    .isNotNull();
        }
    }
}