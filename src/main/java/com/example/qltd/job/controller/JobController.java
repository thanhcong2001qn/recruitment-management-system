package com.example.qltd.job.controller;

import com.example.qltd.common.dto.ApiResponse;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.JobSearchRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
import com.example.qltd.job.dto.response.JobResponse;
import com.example.qltd.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Job", description = "APIs for managing jobs")
public class JobController {

    private final JobService jobService;

    @Operation(summary = "Create job", description = "Create a new job for an active company.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Job created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid job data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody CreateJobRequest request) {

        JobResponse response = jobService.createJob(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<JobResponse>builder()
                                .success(true)
                                .message("Job created successfully")
                                .data(response)
                                .build());
    }

    @Operation(summary = "Search jobs", description = "Search jobs using filters and pagination.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job list retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> searchJobs(
            JobSearchRequest request,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        boolean publicSearch = authentication.getAuthorities()
                .stream()
                .anyMatch(
                        authority -> authority.getAuthority()
                                .equals("ROLE_CANDIDATE"));

        PagedResponse<JobResponse> response = jobService.searchJobs(
                request,
                pageable,
                publicSearch);

        return ResponseEntity.ok(
                ApiResponse.<PagedResponse<JobResponse>>builder()
                        .success(true)
                        .message("Job list retrieved successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Get job by ID", description = "Retrieve a job by ID. Candidates can only view published jobs.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER','CANDIDATE')")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(
            @Parameter(description = "Job ID", example = "1", required = true) @PathVariable Long id,

            Authentication authentication) {

        boolean publicSearch = authentication.getAuthorities()
                .stream()
                .anyMatch(
                        authority -> authority.getAuthority()
                                .equals("ROLE_CANDIDATE"));

        JobResponse response = jobService.getJobById(
                id,
                publicSearch);

        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .success(true)
                        .message("Job retrieved successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Update job", description = "Update an existing job. Closed, expired and archived jobs cannot be updated.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or business rule violation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate slug")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @Parameter(description = "Job ID", example = "1", required = true) @PathVariable Long id,

            @Valid @RequestBody UpdateJobRequest request) {

        JobResponse response = jobService.updateJob(
                id,
                request);

        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .success(true)
                        .message("Job updated successfully")
                        .data(response)
                        .build());
    }
}