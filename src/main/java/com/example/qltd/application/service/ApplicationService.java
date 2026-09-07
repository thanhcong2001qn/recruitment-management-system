package com.example.qltd.application.service;

import com.example.qltd.application.dto.request.ApplicationSearchRequest;
import com.example.qltd.application.dto.request.ChangeApplicationStatusRequest;
import com.example.qltd.application.dto.request.CreateApplicationRequest;
import com.example.qltd.application.dto.response.ApplicationResponse;
import com.example.qltd.common.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

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
            String recruiterEmail,
            ChangeApplicationStatusRequest request);

    ApplicationResponse withdrawApplication(
            Long applicationId,
            String candidateEmail);

    PagedResponse<ApplicationResponse> searchApplications(
            Long jobId,
            String recruiterEmail,
            ApplicationSearchRequest request,
            Pageable pageable);
}