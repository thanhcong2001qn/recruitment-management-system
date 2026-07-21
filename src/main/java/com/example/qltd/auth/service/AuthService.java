package com.example.qltd.auth.service;

import com.example.qltd.auth.dto.request.LoginRequest;
import com.example.qltd.auth.dto.request.RegisterRequest;
import com.example.qltd.auth.dto.response.AuthResponse;
import com.example.qltd.user.dto.respone.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(String email);
}
