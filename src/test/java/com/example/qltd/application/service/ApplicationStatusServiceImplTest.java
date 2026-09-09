package com.example.qltd.application.service;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.service.impl.ApplicationStatusServiceImpl;
import com.example.qltd.application.support.ApplicationTestFactory;
import com.example.qltd.application.validator.ApplicationValidator;
import com.example.qltd.company.entity.Company;
import com.example.qltd.job.entity.Job;
import com.example.qltd.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationStatusService")
class ApplicationStatusServiceImplTest {

    @Mock
    private ApplicationValidator validator;

    @InjectMocks
    private ApplicationStatusServiceImpl statusService;

    private Company company;

    private Job job;

    private User candidate;

    private Application application;

    @BeforeEach
    void setUp() {

        company = ApplicationTestFactory.company();

        job = ApplicationTestFactory
                .publishedJob(company);

        candidate = ApplicationTestFactory
                .candidate(
                        "candidate@test.com");

        application = ApplicationTestFactory
                .application(
                        job,
                        candidate);
    }

    @Test
    @DisplayName("Should transition application successfully")
    void shouldTransitionSuccessfully() {

        statusService.transition(
                application,
                ApplicationStatus.SCREENING);

        assertThat(
                application.getStatus())
                .isEqualTo(
                        ApplicationStatus.SCREENING);

        verify(validator)
                .validateStatusTransition(
                        application,
                        ApplicationStatus.SCREENING);
    }

    @Test
    @DisplayName("Should move application from OFFERED to HIRED")
    void shouldMoveOfferedToHired() {

        application.setStatus(
                ApplicationStatus.OFFERED);

        statusService.transition(
                application,
                ApplicationStatus.HIRED);

        assertThat(
                application.getStatus())
                .isEqualTo(
                        ApplicationStatus.HIRED);

        verify(validator)
                .validateStatusTransition(
                        application,
                        ApplicationStatus.HIRED);
    }

    @Test
    @DisplayName("Should propagate validation exception")
    void shouldPropagateValidationException() {

        doThrow(
                new RuntimeException(
                        "Invalid transition"))
                .when(validator)
                .validateStatusTransition(
                        application,
                        ApplicationStatus.HIRED);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> statusService.transition(
                application,
                ApplicationStatus.HIRED))
                .isInstanceOf(
                        RuntimeException.class)
                .hasMessage(
                        "Invalid transition");

        assertThat(
                application.getStatus())
                .isEqualTo(
                        ApplicationStatus.APPLIED);
    }
}