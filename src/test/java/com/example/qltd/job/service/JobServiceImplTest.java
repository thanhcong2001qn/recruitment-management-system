package com.example.qltd.job.service;

import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.common.mapper.PageMapper;
import com.example.qltd.common.security.CompanyAuthorizationService;
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
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;

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
    private UserRepository userRepository;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private JobValidator jobValidator;

    @Mock
    private JobSlugService jobSlugService;

    @Mock
    private PageMapper pageMapper;

    @Mock
    private JobStatusService jobStatusService;

    @Mock
    private CompanyAuthorizationService companyAuthorizationService;

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

            PagedResponse<JobResponse> result = jobService.searchPublicJobs(
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

            PagedResponse<JobResponse> result = jobService.searchPublicJobs(
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

            jobService.searchPublicJobs(
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

        @Test
        @DisplayName("Should search only published jobs for public API")
        void shouldSearchPublicJobs() {

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

            PagedResponse<JobResponse> result = jobService.searchPublicJobs(
                    request,
                    pageable);

            assertThat(result)
                    .isSameAs(expected);

            verify(jobRepository)
                    .findAll(
                            any(Specification.class),
                            eq(pageable));
        }

        @Test
        @DisplayName("Should search management jobs")
        void shouldSearchManagementJobs() {

            JobSearchRequest request = new JobSearchRequest();

            request.setStatus(
                    JobStatus.DRAFT);

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

            PagedResponse<JobResponse> result = jobService.searchManagementJobs(
                    request,
                    pageable);

            assertThat(result)
                    .isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("getJobById()")
    class GetJobByIdTest {

        @Test
        @DisplayName("Should search management jobs")
        void shouldSearchManagementJobs() {

            JobSearchRequest request = new JobSearchRequest();

            request.setStatus(
                    JobStatus.DRAFT);

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

            PagedResponse<JobResponse> result = jobService.searchManagementJobs(
                    request,
                    pageable);

            assertThat(result)
                    .isSameAs(expected);
        }

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

            JobResponse result = jobService.getPublicJobById(
                    1L);

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

            JobResponse result = jobService.getManagementJobById(
                    1L);

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

            assertThatThrownBy(() -> jobService.getPublicJobById(
                    1L))
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

            assertThatThrownBy(() -> jobService.getPublicJobById(
                    1L))
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

            assertThatThrownBy(() -> jobService.getPublicJobById(
                    1L))
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

            assertThatThrownBy(() -> jobService.getPublicJobById(
                    999L))
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

            assertThatThrownBy(() -> jobService.getPublicJobById(
                    1L))
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

            User recruiter = new User();
            recruiter.setEmail("recruiter@example.com");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository.findByEmailIgnoreCase(
                            "recruiter@example.com"))
                    .thenReturn(
                            Optional.of(recruiter));

            when(
                    jobRepository.save(job))
                    .thenReturn(job);

            when(
                    jobMapper.toResponse(job))
                    .thenReturn(response);

            JobResponse result = jobService.updateJob(
                    1L,
                    "recruiter@example.com",
                    updateRequest);

            assertThat(result)
                    .isSameAs(response);

            verify(jobRepository)
                    .findByIdAndDeletedFalse(1L);

            verify(userRepository)
                    .findByEmailIgnoreCase(
                            "recruiter@example.com");

            verify(companyAuthorizationService)
                    .checkCompanyAccess(
                            recruiter,
                            job.getCompany().getId());

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

            verify(jobMapper)
                    .toResponse(job);
        }

        @Test
        @DisplayName("Should regenerate slug when title changes")
        void shouldRegenerateSlugWhenTitleChanges() {

            job.setTitle(
                    "Java Backend Developer");

            updateRequest.setTitle(
                    "Senior Java Backend Developer");

            User recruiter = new User();
            recruiter.setEmail(
                    "recruiter@example.com");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository.findByEmailIgnoreCase(
                            "recruiter@example.com"))
                    .thenReturn(
                            Optional.of(recruiter));

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
                    "recruiter@example.com",
                    updateRequest);

            assertThat(job.getSlug())
                    .isEqualTo(
                            "senior-java-backend-developer");

            verify(
                    userRepository)
                    .findByEmailIgnoreCase(
                            "recruiter@example.com");

            verify(
                    companyAuthorizationService)
                    .checkCompanyAccess(
                            recruiter,
                            job.getCompany().getId());

            verify(
                    jobSlugService)
                    .generate(
                            "Senior Java Backend Developer",
                            1L);

            verify(
                    jobRepository)
                    .save(job);

            verify(
                    jobMapper)
                    .toResponse(job);
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

            User recruiter = new User();
            recruiter.setEmail(
                    "recruiter@example.com");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository.findByEmailIgnoreCase(
                            "recruiter@example.com"))
                    .thenReturn(
                            Optional.of(recruiter));

            when(
                    jobRepository.save(job))
                    .thenReturn(job);

            when(
                    jobMapper.toResponse(job))
                    .thenReturn(response);

            jobService.updateJob(
                    1L,
                    "recruiter@example.com",
                    updateRequest);

            assertThat(job.getSlug())
                    .isEqualTo(
                            "java-backend-developer");

            verify(companyAuthorizationService)
                    .checkCompanyAccess(
                            recruiter,
                            job.getCompany().getId());

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
                    "recruiter@example.com",
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
                    userRepository,
                    never())
                    .findByEmailIgnoreCase(
                            anyString());

            verify(
                    companyAuthorizationService,
                    never())
                    .checkCompanyAccess(
                            any(),
                            anyLong());

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

            User recruiter = new User();
            recruiter.setEmail(
                    "recruiter@example.com");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository.findByEmailIgnoreCase(
                            "recruiter@example.com"))
                    .thenReturn(
                            Optional.of(recruiter));

            doThrow(
                    new BusinessRuleException(
                            "Job cannot be updated in its current status."))
                    .when(jobValidator)
                    .validateUpdate(
                            job,
                            updateRequest);

            assertThatThrownBy(() -> jobService.updateJob(
                    1L,
                    "recruiter@example.com",
                    updateRequest))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Job cannot be updated in its current status.");

            verify(
                    companyAuthorizationService)
                    .checkCompanyAccess(
                            recruiter,
                            job.getCompany().getId());

            verify(
                    jobValidator)
                    .validateUpdate(
                            job,
                            updateRequest);

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

            User recruiter = new User();
            recruiter.setEmail(
                    "recruiter@example.com");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository.findByEmailIgnoreCase(
                            "recruiter@example.com"))
                    .thenReturn(
                            Optional.of(recruiter));

            doThrow(
                    new BusinessRuleException(
                            "Job cannot be updated in its current status."))
                    .when(jobValidator)
                    .validateUpdate(
                            job,
                            updateRequest);

            assertThatThrownBy(() -> jobService.updateJob(
                    1L,
                    "recruiter@example.com",
                    updateRequest))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Job cannot be updated in its current status.");

            verify(
                    companyAuthorizationService)
                    .checkCompanyAccess(
                            recruiter,
                            job.getCompany().getId());

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
        @DisplayName("Should reject ARCHIVED job")
        void shouldRejectArchivedJob() {

            job.setStatus(
                    JobStatus.ARCHIVED);

            User recruiter = new User();
            recruiter.setEmail(
                    "recruiter@example.com");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository.findByEmailIgnoreCase(
                            "recruiter@example.com"))
                    .thenReturn(
                            Optional.of(recruiter));

            doThrow(
                    new BusinessRuleException(
                            "Job cannot be updated in its current status."))
                    .when(jobValidator)
                    .validateUpdate(
                            job,
                            updateRequest);

            assertThatThrownBy(() -> jobService.updateJob(
                    1L,
                    "recruiter@example.com",
                    updateRequest))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Job cannot be updated in its current status.");

            verify(
                    companyAuthorizationService)
                    .checkCompanyAccess(
                            recruiter,
                            job.getCompany().getId());

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
    }

    @Nested
    @DisplayName("deleteJob()")
    class DeleteJobTest {

        @Test
        @DisplayName("Should soft delete DRAFT job")
        void shouldSoftDeleteDraftJob() {

            job.setStatus(
                    JobStatus.DRAFT);

            job.setDeleted(false);

            User recruiter = new User();
            recruiter.setEmail(
                    "recruiter@example.com");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository.findByEmailIgnoreCase(
                            "recruiter@example.com"))
                    .thenReturn(
                            Optional.of(recruiter));

            when(
                    jobRepository.save(job))
                    .thenReturn(job);

            jobService.deleteJob(
                    1L,
                    "recruiter@example.com");

            assertThat(job.getDeleted())
                    .isTrue();

            verify(
                    userRepository)
                    .findByEmailIgnoreCase(
                            "recruiter@example.com");

            verify(
                    companyAuthorizationService)
                    .checkCompanyAccess(
                            recruiter,
                            job.getCompany().getId());

            verify(
                    jobValidator)
                    .validateDelete(job);

            verify(
                    jobRepository)
                    .save(job);
        }

        @Test
        @DisplayName("Should soft delete CLOSED job")
        void shouldSoftDeleteClosedJob() {

            job.setStatus(
                    JobStatus.CLOSED);

            job.setDeleted(false);

            User recruiter = new User();
            recruiter.setEmail(
                    "recruiter@example.com");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository.findByEmailIgnoreCase(
                            "recruiter@example.com"))
                    .thenReturn(
                            Optional.of(recruiter));

            when(
                    jobRepository.save(job))
                    .thenReturn(job);

            jobService.deleteJob(
                    1L,
                    "recruiter@example.com");

            assertThat(job.getDeleted())
                    .isTrue();

            verify(
                    userRepository)
                    .findByEmailIgnoreCase(
                            "recruiter@example.com");

            verify(
                    companyAuthorizationService)
                    .checkCompanyAccess(
                            recruiter,
                            job.getCompany().getId());

            verify(
                    jobValidator)
                    .validateDelete(job);

            verify(
                    jobRepository)
                    .save(job);
        }

        @Test
        @DisplayName("Should reject deleting PUBLISHED job")
        void shouldRejectDeletingPublishedJob() {

            job.setStatus(
                    JobStatus.PUBLISHED);

            job.setDeleted(false);

            User recruiter = new User();
            recruiter.setEmail(
                    "recruiter@example.com");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository.findByEmailIgnoreCase(
                            "recruiter@example.com"))
                    .thenReturn(
                            Optional.of(recruiter));

            doThrow(
                    new BusinessRuleException(
                            "Published job must be closed before deletion."))
                    .when(jobValidator)
                    .validateDelete(job);

            assertThatThrownBy(() -> jobService.deleteJob(
                    1L,
                    "recruiter@example.com"))
                    .isInstanceOf(
                            BusinessRuleException.class)
                    .hasMessage(
                            "Published job must be closed before deletion.");

            assertThat(job.getDeleted())
                    .isFalse();

            verify(
                    userRepository)
                    .findByEmailIgnoreCase(
                            "recruiter@example.com");

            verify(
                    companyAuthorizationService)
                    .checkCompanyAccess(
                            recruiter,
                            job.getCompany().getId());

            verify(
                    jobValidator)
                    .validateDelete(job);

            verify(
                    jobRepository,
                    never())
                    .save(any());
        }

        @Test
        @DisplayName("Should throw when job does not exist")
        void shouldThrowWhenJobDoesNotExist() {

            when(
                    jobRepository.findByIdAndDeletedFalse(999L))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> jobService.deleteJob(
                    999L,
                    "recruiter@example.com"))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Job not found.");

            verify(
                    userRepository,
                    never())
                    .findByEmailIgnoreCase(
                            anyString());

            verify(
                    companyAuthorizationService,
                    never())
                    .checkCompanyAccess(
                            any(),
                            anyLong());

            verify(
                    jobValidator,
                    never())
                    .validateDelete(any());

            verify(
                    jobRepository,
                    never())
                    .save(any());
        }

        @Test
        @DisplayName("Should not delete already deleted job")
        void shouldNotDeleteAlreadyDeletedJob() {

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> jobService.deleteJob(
                    1L,
                    "recruiter@example.com"))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Job not found.");

            verify(
                    userRepository,
                    never())
                    .findByEmailIgnoreCase(
                            anyString());

            verify(
                    companyAuthorizationService,
                    never())
                    .checkCompanyAccess(
                            any(),
                            anyLong());

            verify(
                    jobValidator,
                    never())
                    .validateDelete(any());

            verify(
                    jobRepository,
                    never())
                    .save(any());
        }
    }

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatusTest {

        @Test
        @DisplayName("Should publish job successfully")
        void shouldPublishJobSuccessfully() {

            job.setStatus(
                    JobStatus.DRAFT);

            User recruiter = new User();
            recruiter.setEmail(
                    "recruiter@example.com");

            when(
                    jobRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(
                            Optional.of(job));

            when(
                    userRepository.findByEmailIgnoreCase(
                            "recruiter@example.com"))
                    .thenReturn(
                            Optional.of(recruiter));

            doAnswer(invocation -> {
                job.setStatus(
                        JobStatus.PUBLISHED);
                return null;
            })
                    .when(jobStatusService)
                    .transition(
                            job,
                            JobStatus.PUBLISHED);

            when(
                    jobRepository.save(job))
                    .thenReturn(job);

            when(
                    jobMapper.toResponse(job))
                    .thenReturn(response);

            JobResponse result = jobService.changeStatus(
                    1L,
                    "recruiter@example.com",
                    JobStatus.PUBLISHED);

            assertThat(result)
                    .isSameAs(response);

            assertThat(job.getStatus())
                    .isEqualTo(
                            JobStatus.PUBLISHED);

            verify(
                    userRepository)
                    .findByEmailIgnoreCase(
                            "recruiter@example.com");

            verify(
                    companyAuthorizationService)
                    .checkCompanyAccess(
                            recruiter,
                            job.getCompany().getId());

            verify(
                    jobStatusService)
                    .transition(
                            job,
                            JobStatus.PUBLISHED);

            verify(
                    jobRepository)
                    .save(job);

            verify(
                    jobMapper)
                    .toResponse(job);
        }

        @Test
        @DisplayName("Should throw when job does not exist")
        void shouldThrowWhenJobDoesNotExist() {

            when(
                    jobRepository.findByIdAndDeletedFalse(999L))
                    .thenReturn(
                            Optional.empty());

            assertThatThrownBy(() -> jobService.changeStatus(
                    999L,
                    "recruiter@example.com",
                    JobStatus.PUBLISHED))
                    .isInstanceOf(
                            ResourceNotFoundException.class)
                    .hasMessage(
                            "Job not found.");

            verify(
                    userRepository,
                    never())
                    .findByEmailIgnoreCase(
                            anyString());

            verify(
                    companyAuthorizationService,
                    never())
                    .checkCompanyAccess(
                            any(),
                            anyLong());

            verify(
                    jobStatusService,
                    never())
                    .transition(
                            any(),
                            any());

            verify(
                    jobRepository,
                    never())
                    .save(any());

            verify(
                    jobMapper,
                    never())
                    .toResponse(any());
        }
    }
}