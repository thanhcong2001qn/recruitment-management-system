package com.example.qltd.job.service;

import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.common.mapper.PageMapper;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.JobSearchRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
import com.example.qltd.job.dto.response.JobResponse;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
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
import static org.mockito.ArgumentMatchers.anyLong;
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
                    pageable, true);

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
                    pageable, true);

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
                    pageable, true);

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

        @Test
        @DisplayName("Should search only public jobs when publicSearch is true")
        void shouldSearchPublicJobsOnly() {

            JobSearchRequest request = new JobSearchRequest();

            Pageable pageable = PageRequest.of(0, 10);

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
                    pageable,
                    true);

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
        @DisplayName("Should search all management jobs when publicSearch is false")
        void shouldSearchManagementJobs() {

            JobSearchRequest request = new JobSearchRequest();

            Pageable pageable = PageRequest.of(0, 10);

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
                    pageable,
                    false);

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
    }

    @Nested
    @DisplayName("getJobById()")
    class GetJobByIdTest {

        @Test
        @DisplayName("Should return PUBLISHED job for candidate")
        void shouldReturnPublishedJobForCandidate() {

            job.setStatus(JobStatus.PUBLISHED);

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    jobMapper.toResponse(job))
                    .thenReturn(response);

            JobResponse result = jobService.getJobById(
                    1L,
                    true);

            assertThat(result)
                    .isSameAs(response);

            verify(jobRepository)
                    .findByIdAndDeletedFalse(1L);

            verify(jobMapper)
                    .toResponse(job);
        }

        @Test
        @DisplayName("Should return any active job for management user")
        void shouldReturnActiveJobForManagementUser() {

            job.setStatus(JobStatus.DRAFT);

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    jobMapper.toResponse(job))
                    .thenReturn(response);

            JobResponse result = jobService.getJobById(
                    1L,
                    false);

            assertThat(result)
                    .isSameAs(response);

            verify(jobMapper)
                    .toResponse(job);
        }

        @Test
        @DisplayName("Should reject non-published job for candidate")
        void shouldRejectNonPublishedJobForCandidate() {

            job.setStatus(JobStatus.DRAFT);

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            assertThatThrownBy(() -> jobService.getJobById(
                    1L,
                    true))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Job not found.");

            verify(
                    jobMapper,
                    never())
                    .toResponse(any());
        }

        @Test
        @DisplayName("Should reject CLOSED job for candidate")
        void shouldRejectClosedJobForCandidate() {

            job.setStatus(JobStatus.CLOSED);

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            assertThatThrownBy(() -> jobService.getJobById(
                    1L,
                    true))
                    .isInstanceOf(
                            ResourceNotFoundException.class);

            verify(
                    jobMapper,
                    never())
                    .toResponse(any());
        }

        @Test
        @DisplayName("Should reject EXPIRED job for candidate")
        void shouldRejectExpiredJobForCandidate() {

            job.setStatus(JobStatus.EXPIRED);

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            assertThatThrownBy(() -> jobService.getJobById(
                    1L,
                    true))
                    .isInstanceOf(
                            ResourceNotFoundException.class);

            verify(
                    jobMapper,
                    never())
                    .toResponse(any());
        }

        @Test
        @DisplayName("Should throw when job does not exist")
        void shouldThrowWhenJobDoesNotExist() {

            when(
                    jobRepository.findByIdAndDeletedFalse(999L))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> jobService.getJobById(
                    999L,
                    false))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Job not found.");

            verify(
                    jobMapper,
                    never())
                    .toResponse(any());
        }

        @Test
        @DisplayName("Should not return soft deleted job")
        void shouldNotReturnSoftDeletedJob() {

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> jobService.getJobById(
                    1L,
                    false))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Job not found.");

            verify(
                    jobRepository)
                    .findByIdAndDeletedFalse(1L);
        }
    }

    @Nested
    @DisplayName("updateJob()")
    class UpdateJobTest {

        private UpdateJobRequest updateRequest;

        @BeforeEach
        void setUpUpdateRequest() {

            updateRequest = JobTestFactory.updateRequest();

            job.setStatus(
                    JobStatus.DRAFT);
        }

        @Test
        @DisplayName("Should update DRAFT job successfully")
        void shouldUpdateDraftJobSuccessfully() {

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    jobRepository.save(job))
                    .thenReturn(job);

            when(
                    jobMapper.toResponse(job))
                    .thenReturn(response);

            JobResponse result = jobService.updateJob(
                    1L,
                    updateRequest);

            assertThat(result)
                    .isSameAs(response);

            verify(jobValidator)
                    .validateUpdate(
                            job,
                            updateRequest);

            verify(jobMapper)
                    .updateEntity(
                            job,
                            updateRequest);

            verify(jobRepository)
                    .save(job);
        }

        @Test
        @DisplayName("Should regenerate slug when title changes")
        void shouldRegenerateSlugWhenTitleChanges() {

            job.setTitle(
                    "Java Backend Developer");

            updateRequest.setTitle(
                    "Senior Java Backend Developer");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    jobSlugService.generate(
                            "Senior Java Backend Developer",
                            1L))
                    .thenReturn(
                            "senior-java-backend-developer");

            when(
                    jobRepository.save(job))
                    .thenReturn(job);

            when(
                    jobMapper.toResponse(job))
                    .thenReturn(response);

            jobService.updateJob(
                    1L,
                    updateRequest);

            assertThat(job.getSlug())
                    .isEqualTo(
                            "senior-java-backend-developer");

            verify(jobSlugService)
                    .generate(
                            "Senior Java Backend Developer",
                            1L);
        }

        @Test
        @DisplayName("Should keep slug when title does not change")
        void shouldKeepSlugWhenTitleDoesNotChange() {

            job.setTitle(
                    "Java Backend Developer");

            job.setSlug(
                    "java-backend-developer");

            updateRequest.setTitle(
                    "Java Backend Developer");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    jobRepository.save(job))
                    .thenReturn(job);

            when(
                    jobMapper.toResponse(job))
                    .thenReturn(response);

            jobService.updateJob(
                    1L,
                    updateRequest);

            assertThat(job.getSlug())
                    .isEqualTo(
                            "java-backend-developer");

            verify(
                    jobSlugService,
                    never())
                    .generate(
                            anyString(),
                            anyLong());
        }

        @Test
        @DisplayName("Should throw when job does not exist")
        void shouldThrowWhenJobDoesNotExist() {

            when(
                    jobRepository.findByIdAndDeletedFalse(999L))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> jobService.updateJob(
                    999L,
                    updateRequest))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Job not found.");

            verify(
                    jobValidator,
                    never())
                    .validateUpdate(
                            any(),
                            any());

            verify(
                    jobRepository,
                    never())
                    .save(any());
        }

        @Test
        @DisplayName("Should reject CLOSED job")
        void shouldRejectClosedJob() {

            job.setStatus(
                    JobStatus.CLOSED);

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            doThrow(
                    new BusinessRuleException(
                            "Job cannot be updated in its current status."))
                    .when(jobValidator)
                    .validateUpdate(
                            job,
                            updateRequest);

            assertThatThrownBy(() -> jobService.updateJob(
                    1L,
                    updateRequest))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Job cannot be updated in its current status.");

            verify(
                    jobRepository,
                    never())
                    .save(any());

            verify(
                    jobSlugService,
                    never())
                    .generate(
                            anyString(),
                            anyLong());
        }

        @Test
        @DisplayName("Should reject EXPIRED job")
        void shouldRejectExpiredJob() {

            job.setStatus(
                    JobStatus.EXPIRED);

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            doThrow(
                    new BusinessRuleException(
                            "Job cannot be updated in its current status."))
                    .when(jobValidator)
                    .validateUpdate(
                            job,
                            updateRequest);

            assertThatThrownBy(() -> jobService.updateJob(
                    1L,
                    updateRequest))
                    .isInstanceOf(
                            BusinessRuleException.class);

            verify(
                    jobRepository,
                    never())
                    .save(any());
        }

        @Test
        @DisplayName("Should reject ARCHIVED job")
        void shouldRejectArchivedJob() {

            job.setStatus(
                    JobStatus.ARCHIVED);

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            doThrow(
                    new BusinessRuleException(
                            "Job cannot be updated in its current status."))
                    .when(jobValidator)
                    .validateUpdate(
                            job,
                            updateRequest);

            assertThatThrownBy(() -> jobService.updateJob(
                    1L,
                    updateRequest))
                    .isInstanceOf(
                            BusinessRuleException.class);

            verify(
                    jobRepository,
                    never())
                    .save(any());
        }
    }
}