package com.example.qltd.job.service;

import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.service.impl.JobStatusServiceImpl;
import com.example.qltd.job.validator.JobValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobStatusService")
class JobStatusServiceImplTest {

    @Mock
    private JobValidator jobValidator;

    @InjectMocks
    private JobStatusServiceImpl jobStatusService;

    private Job job;

    @BeforeEach
    void setUp() {

        job = Job.builder()
                .id(1L)
                .title("Java Backend Developer")
                .status(JobStatus.DRAFT)
                .deadline(
                        LocalDate.now().plusDays(30))
                .build();
    }

    @Nested
    @DisplayName("DRAFT -> PUBLISHED")
    class PublishTest {

        @Test
        @DisplayName("Should publish DRAFT job successfully")
        void shouldPublishDraftJobSuccessfully() {

            doNothing()
                    .when(jobValidator)
                    .validatePublish(job);

            jobStatusService.transition(
                    job,
                    JobStatus.PUBLISHED);

            assertThat(job.getStatus())
                    .isEqualTo(JobStatus.PUBLISHED);

            verify(jobValidator)
                    .validatePublish(job);
        }

        @Test
        @DisplayName("Should not publish when validation fails")
        void shouldNotPublishWhenValidationFails() {

            doThrow(
                    new BusinessRuleException(
                            "Job deadline must not be in the past."))
                    .when(jobValidator)
                    .validatePublish(job);

            assertThatThrownBy(() -> jobStatusService.transition(
                    job,
                    JobStatus.PUBLISHED))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Job deadline must not be in the past.");

            assertThat(job.getStatus())
                    .isEqualTo(JobStatus.DRAFT);

            verify(jobValidator)
                    .validatePublish(job);
        }
    }

    @Nested
    @DisplayName("PUBLISHED -> CLOSED")
    class CloseTest {

        @BeforeEach
        void setUp() {

            job.setStatus(JobStatus.PUBLISHED);
        }

        @Test
        @DisplayName("Should close PUBLISHED job successfully")
        void shouldClosePublishedJobSuccessfully() {

            jobStatusService.transition(
                    job,
                    JobStatus.CLOSED);

            assertThat(job.getStatus())
                    .isEqualTo(JobStatus.CLOSED);

            verify(jobValidator, never())
                    .validatePublish(job);
        }

        @Test
        @DisplayName("Should reject closing DRAFT job")
        void shouldRejectClosingDraftJob() {

            job.setStatus(JobStatus.DRAFT);

            assertThatThrownBy(() -> jobStatusService.transition(
                    job,
                    JobStatus.CLOSED))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Only PUBLISHED jobs can be closed.");

            assertThat(job.getStatus())
                    .isEqualTo(JobStatus.DRAFT);
        }
    }

    @Nested
    @DisplayName("PUBLISHED -> EXPIRED")
    class ExpireTest {

        @BeforeEach
        void setUp() {

            job.setStatus(JobStatus.PUBLISHED);
        }

        @Test
        @DisplayName("Should expire PUBLISHED job successfully")
        void shouldExpirePublishedJobSuccessfully() {

            jobStatusService.transition(
                    job,
                    JobStatus.EXPIRED);

            assertThat(job.getStatus())
                    .isEqualTo(JobStatus.EXPIRED);
        }

        @Test
        @DisplayName("Should reject expiring DRAFT job")
        void shouldRejectExpiringDraftJob() {

            job.setStatus(JobStatus.DRAFT);

            assertThatThrownBy(() -> jobStatusService.transition(
                    job,
                    JobStatus.EXPIRED))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Only PUBLISHED jobs can expire.");
        }
    }

    @Nested
    @DisplayName("CLOSED / EXPIRED -> ARCHIVED")
    class ArchiveTest {

        @Test
        @DisplayName("Should archive CLOSED job")
        void shouldArchiveClosedJob() {

            job.setStatus(JobStatus.CLOSED);

            jobStatusService.transition(
                    job,
                    JobStatus.ARCHIVED);

            assertThat(job.getStatus())
                    .isEqualTo(JobStatus.ARCHIVED);
        }

        @Test
        @DisplayName("Should archive EXPIRED job")
        void shouldArchiveExpiredJob() {

            job.setStatus(JobStatus.EXPIRED);

            jobStatusService.transition(
                    job,
                    JobStatus.ARCHIVED);

            assertThat(job.getStatus())
                    .isEqualTo(JobStatus.ARCHIVED);
        }

        @Test
        @DisplayName("Should reject archiving DRAFT job")
        void shouldRejectArchivingDraftJob() {

            job.setStatus(JobStatus.DRAFT);

            assertThatThrownBy(() -> jobStatusService.transition(
                    job,
                    JobStatus.ARCHIVED))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Only CLOSED or EXPIRED jobs can be archived.");
        }

        @Test
        @DisplayName("Should reject archiving PUBLISHED job")
        void shouldRejectArchivingPublishedJob() {

            job.setStatus(JobStatus.PUBLISHED);

            assertThatThrownBy(() -> jobStatusService.transition(
                    job,
                    JobStatus.ARCHIVED))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "Only CLOSED or EXPIRED jobs can be archived.");
        }
    }

    @Nested
    @DisplayName("Invalid transitions")
    class InvalidTransitionTest {

        @Test
        @DisplayName("Should reject CLOSED -> PUBLISHED")
        void shouldRejectClosedToPublished() {
            job.setStatus(JobStatus.CLOSED);
            assertThatThrownBy(() -> jobStatusService.transition(job, JobStatus.PUBLISHED))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Only DRAFT jobs can be published.");
        }

        @Test
        @DisplayName("Should reject EXPIRED -> PUBLISHED")
        void shouldRejectExpiredToPublished() {
            job.setStatus(JobStatus.EXPIRED);
            assertThatThrownBy(() -> jobStatusService.transition(job, JobStatus.PUBLISHED))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Only DRAFT jobs can be published.");
        }

        @Test
        @DisplayName("Should reject ARCHIVED -> PUBLISHED")
        void shouldRejectArchivedToPublished() {
            job.setStatus(JobStatus.ARCHIVED);
            assertThatThrownBy(() -> jobStatusService.transition(job, JobStatus.PUBLISHED))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Only DRAFT jobs can be published.");
        }

        @Test
        @DisplayName("Should reject any transition back to DRAFT")
        void shouldRejectTransitionToDraft() {

            job.setStatus(JobStatus.PUBLISHED);

            assertThatThrownBy(() -> jobStatusService.transition(
                    job,
                    JobStatus.DRAFT))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(
                            "A Job cannot be moved back to DRAFT.");
        }
    }

    @Test
    @DisplayName("Should do nothing when target status is current status")
    void shouldDoNothingWhenStatusIsAlreadyCurrent() {

        job.setStatus(JobStatus.PUBLISHED);

        jobStatusService.transition(
                job,
                JobStatus.PUBLISHED);

        assertThat(job.getStatus())
                .isEqualTo(JobStatus.PUBLISHED);
    }
}