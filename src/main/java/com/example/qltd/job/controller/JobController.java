package com.example.qltd.job.controller;

import com.example.qltd.common.dto.ApiResponse;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.job.dto.request.CreateJobRequest;
import com.example.qltd.job.dto.request.JobSearchRequest;
import com.example.qltd.job.dto.request.UpdateJobRequest;
import com.example.qltd.job.dto.response.JobResponse;
import com.example.qltd.job.enums.JobStatus;
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

    @Operation(summary = "Search published jobs", description = "Search jobs available to candidates. Only PUBLISHED jobs are returned.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Published jobs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER','CANDIDATE')")
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> searchPublicJobs(

            JobSearchRequest request,

            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PagedResponse<JobResponse> response = jobService.searchPublicJobs(
                request,
                pageable);

        return ResponseEntity.ok(
                ApiResponse.<PagedResponse<JobResponse>>builder()
                        .success(true)
                        .message(
                                "Published job list retrieved successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Get published job details", description = "Retrieve details of a published job.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Published job not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER','CANDIDATE')")
    public ResponseEntity<ApiResponse<JobResponse>> getPublicJob(
            @PathVariable Long id) {

        JobResponse response = jobService.getPublicJobById(id);

        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .success(true)
                        .message(
                                "Job retrieved successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Search jobs for management", description = "Search all non-deleted jobs for ADMIN and RECRUITER.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jobs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/manage")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> searchManagementJobs(

            JobSearchRequest request,

            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PagedResponse<JobResponse> response = jobService.searchManagementJobs(
                request,
                pageable);

        return ResponseEntity.ok(
                ApiResponse.<PagedResponse<JobResponse>>builder()
                        .success(true)
                        .message(
                                "Job list retrieved successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Get job details for management", description = "Retrieve a non-deleted job regardless of its status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/manage/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> getManagementJob(
            @PathVariable Long id) {

        JobResponse response = jobService.getManagementJobById(id);

        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .success(true)
                        .message(
                                "Job retrieved successfully")
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

    @Operation(summary = "Delete job", description = "Soft delete a job. Published jobs must be closed before deletion.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Job cannot be deleted in its current status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only ADMIN or RECRUITER can delete jobs"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @Parameter(description = "Job ID", example = "1", required = true) @PathVariable Long id) {

        jobService.deleteJob(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Job deleted successfully")
                        .build());
    }

    @Operation(summary = "Publish job", description = "Publish a DRAFT job.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job published successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Job cannot be published"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> publishJob(
            @PathVariable Long id) {

        JobResponse response = jobService.changeStatus(
                id,
                JobStatus.PUBLISHED);

        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .success(true)
                        .message("Job published successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Close job", description = "Close a PUBLISHED job.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job closed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Job cannot be closed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> closeJob(
            @PathVariable Long id) {

        JobResponse response = jobService.changeStatus(
                id,
                JobStatus.CLOSED);

        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .success(true)
                        .message("Job closed successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Expire job", description = "Mark a PUBLISHED job as EXPIRED.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job expired successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Job cannot be expired"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping("/{id}/expire")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> expireJob(
            @PathVariable Long id) {

        JobResponse response = jobService.changeStatus(
                id,
                JobStatus.EXPIRED);

        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .success(true)
                        .message("Job expired successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Archive job", description = "Archive a CLOSED or EXPIRED job.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job archived successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Job cannot be archived"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> archiveJob(
            @PathVariable Long id) {

        JobResponse response = jobService.changeStatus(
                id,
                JobStatus.ARCHIVED);

        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .success(true)
                        .message("Job archived successfully")
                        .data(response)
                        .build());
    }
}