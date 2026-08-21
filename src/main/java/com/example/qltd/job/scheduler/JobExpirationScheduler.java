package com.example.qltd.job.scheduler;

import com.example.qltd.job.service.JobExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobExpirationScheduler {

    private final JobExpirationService jobExpirationService;

    @Scheduled(cron = "${job.expiration.cron:0 0 0 * * *}", zone = "${job.expiration.zone:Asia/Ho_Chi_Minh}")
    public void expireJobs() {

        int expiredCount = jobExpirationService.expireJobs();

        log.info(
                "Job expiration scheduler completed. Expired jobs: {}",
                expiredCount);
    }
}