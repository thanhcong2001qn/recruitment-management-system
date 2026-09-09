package com.example.qltd.job.service;

import com.example.qltd.company.entity.Company;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.job.service.impl.JobExpirationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobExpirationService")
class JobExpirationServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobStatusService jobStatusService;

    private Clock clock;

    private JobExpirationServiceImpl jobExpirationService;

    private Company company;

    @BeforeEach
    void setUp() {

        clock = Clock.fixed(
                Instant.parse("2026-08-21T00:00:00Z"),
                ZoneId.of("Asia/Ho_Chi_Minh"));

        jobExpirationService = new JobExpirationServiceImpl(
                jobRepository,
                jobStatusService,
                clock);

        company = Company.builder()
                .id(1L)
                .name("OpenAI")
                .slug("openai")
                .deleted(false)
                .build();
    }

    @Test
    @DisplayName("Should expire all overdue published jobs")
    void shouldExpireAllOverduePublishedJobs() {

        Job job1 = Job.builder()
                .id(1L)
                .title("Java Developer")
                .slug("java-developer")
                .status(JobStatus.PUBLISHED)
                .deadline(
                        LocalDate.of(
                                2026,
                                8,
                                19))
                .deleted(false)
                .company(company)
                .build();

        Job job2 = Job.builder()
                .id(2L)
                .title("Backend Developer")
                .slug("backend-developer")
                .status(JobStatus.PUBLISHED)
                .deadline(
                        LocalDate.of(
                                2026,
                                8,
                                20))
                .deleted(false)
                .company(company)
                .build();

        when(
                jobRepository
                        .findAllByStatusAndDeadlineBeforeAndDeletedFalse(
                                JobStatus.PUBLISHED,
                                LocalDate.of(
                                        2026,
                                        8,
                                        21)))
                .thenReturn(
                        List.of(
                                job1,
                                job2));

        int result = jobExpirationService.expireJobs();

        assertThat(result)
                .isEqualTo(2);

        verify(jobStatusService)
                .transition(
                        job1,
                        JobStatus.EXPIRED);

        verify(jobStatusService)
                .transition(
                        job2,
                        JobStatus.EXPIRED);
    }

    @Test
    @DisplayName("Should return zero when no jobs are overdue")
    void shouldReturnZeroWhenNoJobsAreOverdue() {

        when(
                jobRepository
                        .findAllByStatusAndDeadlineBeforeAndDeletedFalse(
                                JobStatus.PUBLISHED,
                                LocalDate.of(
                                        2026,
                                        8,
                                        21)))
                .thenReturn(
                        List.of());

        int result = jobExpirationService.expireJobs();

        assertThat(result)
                .isZero();

        verifyNoInteractions(
                jobStatusService);
    }

    @Test
    @DisplayName("Should not query non-published jobs")
    void shouldOnlyQueryPublishedJobs() {

        when(
                jobRepository
                        .findAllByStatusAndDeadlineBeforeAndDeletedFalse(
                                JobStatus.PUBLISHED,
                                LocalDate.of(
                                        2026,
                                        8,
                                        21)))
                .thenReturn(
                        List.of());

        jobExpirationService.expireJobs();

        verify(
                jobRepository)
                .findAllByStatusAndDeadlineBeforeAndDeletedFalse(
                        JobStatus.PUBLISHED,
                        LocalDate.of(
                                2026,
                                8,
                                21));
    }
}