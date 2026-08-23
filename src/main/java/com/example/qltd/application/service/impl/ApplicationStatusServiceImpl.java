package com.example.qltd.application.service.impl;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.application.service.ApplicationStatusService;
import com.example.qltd.application.validator.ApplicationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationStatusServiceImpl
        implements ApplicationStatusService {

    private final ApplicationValidator validator;

    @Override
    public void transition(
            Application application,
            ApplicationStatus targetStatus) {

        validator.validateStatusTransition(
                application,
                targetStatus);

        application.setStatus(
                targetStatus);
    }
}