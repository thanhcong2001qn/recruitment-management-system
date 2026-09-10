package com.example.qltd.interview.controller;

import com.example.qltd.common.dto.ApiResponse;
import com.example.qltd.interview.dto.request.ChangeInterviewStatusRequest;
import com.example.qltd.interview.dto.request.CreateInterviewRequest;
import com.example.qltd.interview.dto.request.UpdateInterviewFeedbackRequest;
import com.example.qltd.interview.dto.response.InterviewResponse;
import com.example.qltd.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Interview", description = "APIs for recruitment interviews")
public class InterviewController {

    private final InterviewService interviewService;

    @Operation(summary = "Get application interviews", description = "Get all interview rounds for an application as an admin or authorized recruiter.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Application interviews retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Application or user not found")
    })
    @GetMapping("/applications/{applicationId}/interviews")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getApplicationInterviews(
            @PathVariable Long applicationId,
            Authentication authentication) {

        List<InterviewResponse> response = interviewService
                .getApplicationInterviews(
                        applicationId,
                        authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.<List<InterviewResponse>>builder()
                        .success(true)
                        .message("Application interviews retrieved successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Schedule an interview", description = "Schedule an interview round for an application as an admin or authorized recruiter.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Interview scheduled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid schedule or application status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Application or user not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Interview round already exists")
    })
    @PostMapping("/applications/{applicationId}/interviews")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<InterviewResponse>> scheduleInterview(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateInterviewRequest request,
            Authentication authentication) {

        InterviewResponse response = interviewService.scheduleInterview(
                applicationId,
                authentication.getName(),
                request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<InterviewResponse>builder()
                                .success(true)
                                .message("Interview scheduled successfully")
                                .data(response)
                                .build());
    }

    @Operation(summary = "Change interview status", description = "Update an interview through its allowed status lifecycle as an admin or authorized recruiter.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interview status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Interview or user not found")
    })
    @PatchMapping("/interviews/{interviewId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<InterviewResponse>> changeStatus(
            @PathVariable Long interviewId,
            @Valid @RequestBody ChangeInterviewStatusRequest request,
            Authentication authentication) {

        InterviewResponse response = interviewService.changeStatus(
                interviewId,
                authentication.getName(),
                request);

        return ResponseEntity.ok(
                ApiResponse.<InterviewResponse>builder()
                        .success(true)
                        .message("Interview status updated successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Update interview feedback", description = "Add or replace feedback and rating for a completed interview as an admin or authorized recruiter.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interview feedback updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid feedback or interview status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Interview or user not found")
    })
    @PutMapping("/interviews/{interviewId}/feedback")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<InterviewResponse>> updateFeedback(
            @PathVariable Long interviewId,
            @Valid @RequestBody UpdateInterviewFeedbackRequest request,
            Authentication authentication) {

        InterviewResponse response = interviewService.updateFeedback(
                interviewId,
                authentication.getName(),
                request);

        return ResponseEntity.ok(
                ApiResponse.<InterviewResponse>builder()
                        .success(true)
                        .message("Interview feedback updated successfully")
                        .data(response)
                        .build());
    }
}
