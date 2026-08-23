package com.example.qltd.application.service;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;

public interface ApplicationStatusService {

    void transition(
            Application application,
            ApplicationStatus targetStatus);
}