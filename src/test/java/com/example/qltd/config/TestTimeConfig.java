package com.example.qltd.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class TestTimeConfig {

    @Bean
    public Clock testClock() {

        return Clock.fixed(
                Instant.parse(
                        "2026-08-21T00:00:00Z"),
                ZoneId.of(
                        "Asia/Ho_Chi_Minh"));
    }
}