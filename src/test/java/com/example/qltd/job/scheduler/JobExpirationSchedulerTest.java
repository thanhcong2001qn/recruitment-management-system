package com.example.qltd.job.scheduler;

import com.example.qltd.job.service.JobExpirationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobExpirationScheduler")
class JobExpirationSchedulerTest {

    @Mock
    private JobExpirationService jobExpirationService;

    @InjectMocks
    private JobExpirationScheduler scheduler;

    @Test
    @DisplayName("Should trigger job expiration service")
    void shouldTriggerJobExpirationService() {

        when(
                jobExpirationService.expireJobs())
                .thenReturn(3);

        scheduler.expireJobs();

        verify(
                jobExpirationService)
                .expireJobs();
    }
}