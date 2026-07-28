package com.example.qltd.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.qltd.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/test")
public class TestAuthorizationController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminOnly() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Admin access granted")
                        .data("Hello ADMIN")
                        .build());
    }

    @GetMapping("/recruiter")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    public ResponseEntity<ApiResponse<String>> recruiterOnly() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Recruiter access granted")
                        .data("Hello RECRUITER")
                        .build());
    }

    @GetMapping("/candidate")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<String>> candidateOnly() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Candidate access granted")
                        .data("Hello CANDIDATE")
                        .build());
    }

    @GetMapping("/authenticated")
    public ResponseEntity<ApiResponse<String>> authenticatedOnly() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Authenticated access granted")
                        .data("Hello authenticated user")
                        .build());
    }
}
