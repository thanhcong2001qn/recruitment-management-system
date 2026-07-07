package com.example.qltd.dto.response;

import com.example.qltd.enums.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String accessToken;

    private String tokenType;

    private Long userId;

    private String fullName;

    private String email;

    private Role role;
}
