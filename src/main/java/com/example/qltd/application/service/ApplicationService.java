package com.example.qltd.application.service;

import com.example.qltd.application.dto.request.ChangeApplicationStatusRequest;
import com.example.qltd.application.dto.request.CreateApplicationRequest;
import com.example.qltd.application.dto.response.ApplicationResponse;

public interface ApplicationService {

    ApplicationResponse createApplication(
            Long jobId,
            String candidateEmail,
            CreateApplicationRequest request);

    ApplicationResponse getMyApplication(
            Long applicationId,
            String candidateEmail);

    ApplicationResponse changeStatus(
            Long applicationId,
            ChangeApplicationStatusRequest request);

    ApplicationResponse withdrawApplication(
            Long applicationId,
            String candidateEmail);
}