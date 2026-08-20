package com.example.qltd.job.controller;

import com.example.qltd.common.dto.ApiResponse;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.JobSearchRequest;
import com.example.qltd.job.dto.response.JobResponse;
import com.example.qltd.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER','CANDIDATE')")
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> searchJobs(

            JobSearchRequest request,

            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PagedResponse<JobResponse> response = jobService.searchJobs(
                request,
                pageable);

        return ResponseEntity.ok(
                ApiResponse.<PagedResponse<JobResponse>>builder()
                        .success(true)
                        .message("Job list retrieved successfully")
                        .data(response)
                        .build());
    }
}