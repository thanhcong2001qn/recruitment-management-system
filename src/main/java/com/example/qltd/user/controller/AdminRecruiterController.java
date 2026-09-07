package com.example.qltd.user.controller;

import com.example.qltd.common.dto.ApiResponse;
import com.example.qltd.user.dto.request.AssignRecruiterCompanyRequest;
import com.example.qltd.user.dto.respone.RecruiterResponse;
import com.example.qltd.user.service.RecruiterManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/recruiters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Recruiter Management", description = "APIs for managing recruiter assignments")
public class AdminRecruiterController {

    private final RecruiterManagementService recruiterManagementService;

    @Operation(summary = "Assign recruiter to company", description = "Assign or move a recruiter to an active company.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recruiter assigned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "User is not a recruiter or company is inactive"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Administrator role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recruiter or company not found")
    })
    @PatchMapping("/{recruiterId}/company")
    public ResponseEntity<ApiResponse<RecruiterResponse>> assignCompany(
            @PathVariable Long recruiterId,
            @Valid @RequestBody AssignRecruiterCompanyRequest request) {

        RecruiterResponse response = recruiterManagementService.assignCompany(
                recruiterId,
                request.getCompanyId());

        return ResponseEntity.ok(
                ApiResponse.<RecruiterResponse>builder()
                        .success(true)
                        .message("Recruiter assigned to company successfully")
                        .data(response)
                        .build());
    }
}
