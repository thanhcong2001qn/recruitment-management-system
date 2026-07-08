package com.example.qltd.service;

import com.example.qltd.dto.request.LoginRequest;
import com.example.qltd.dto.request.RegisterRequest;
import com.example.qltd.dto.response.AuthResponse;
import com.example.qltd.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(String email);
}
