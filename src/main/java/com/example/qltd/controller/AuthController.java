package com.example.qltd.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.qltd.dto.request.LoginRequest;
import com.example.qltd.dto.request.RegisterRequest;
import com.example.qltd.dto.response.ApiResponse;
import com.example.qltd.dto.response.AuthResponse;
import com.example.qltd.dto.response.UserResponse;
import com.example.qltd.exception.UnauthorizedException;
import com.example.qltd.security.CustomUserDetails;
import com.example.qltd.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Register successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successfully", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
                Authentication authentication
        ) {
        if (authentication == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails currentUser)
                || currentUser.getUser() == null) {
            throw new UnauthorizedException("Authentication is required");
        }

        UserResponse response = authService.getCurrentUser(
                currentUser.getUsername()
        );

        return ResponseEntity.ok(ApiResponse.success("Get current user successfully", response));
    }
}
