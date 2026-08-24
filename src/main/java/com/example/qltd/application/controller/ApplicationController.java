package com.example.qltd.application.controller;

import com.example.qltd.application.dto.request.ApplicationSearchRequest;
import com.example.qltd.application.dto.request.ChangeApplicationStatusRequest;
import com.example.qltd.application.dto.request.CreateApplicationRequest;
import com.example.qltd.application.dto.response.ApplicationResponse;
import com.example.qltd.application.service.ApplicationService;
import com.example.qltd.common.dto.ApiResponse;
import com.example.qltd.common.dto.PagedResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Application", description = "APIs for job applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "Apply for a job", description = "Create a job application as the authenticated candidate.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Application created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Job is not accepting applications"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only candidates can apply"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job or candidate not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Candidate already applied")
    })
    @PostMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
            @PathVariable Long jobId,
            @Valid @RequestBody CreateApplicationRequest request,
            Authentication authentication) {

        ApplicationResponse response = applicationService.createApplication(
                jobId,
                authentication.getName(),
                request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<ApplicationResponse>builder()
                                .success(true)
                                .message(
                                        "Application created successfully")
                                .data(response)
                                .build());
    }

    @Operation(summary = "Get my application", description = "Get an application belonging to the authenticated candidate.")
    @GetMapping("/applications/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getMyApplication(
            @PathVariable Long id,
            Authentication authentication) {

        ApplicationResponse response = applicationService.getMyApplication(
                id,
                authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.<ApplicationResponse>builder()
                        .success(true)
                        .message(
                                "Application retrieved successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Change application status", description = "Recruiter or admin updates application workflow status.")
    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeApplicationStatusRequest request) {

        ApplicationResponse response = applicationService.changeStatus(
                id,
                request);

        return ResponseEntity.ok(
                ApiResponse.<ApplicationResponse>builder()
                        .success(true)
                        .message(
                                "Application status updated successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Withdraw application", description = "Withdraw the authenticated candidate's application.")
    @PostMapping("/applications/{id}/withdraw")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> withdraw(
            @PathVariable Long id,
            Authentication authentication) {

        ApplicationResponse response = applicationService.withdrawApplication(
                id,
                authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.<ApplicationResponse>builder()
                        .success(true)
                        .message(
                                "Application withdrawn successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Search applications of a job", description = "Search and paginate applications belonging to a specific job.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only admin and recruiter can access"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationResponse>>> searchApplications(

            @PathVariable Long jobId,

            ApplicationSearchRequest request,

            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PagedResponse<ApplicationResponse> response = applicationService.searchApplications(
                jobId,
                request,
                pageable);

        return ResponseEntity.ok(
                ApiResponse.<PagedResponse<ApplicationResponse>>builder()
                        .success(true)
                        .message(
                                "Applications retrieved successfully")
                        .data(response)
                        .build());
    }
}