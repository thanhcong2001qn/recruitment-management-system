package com.example.qltd.dto.response;

import com.example.qltd.enums.Role;
import com.example.qltd.enums.UserStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private Role role;

    private UserStatus status;
}
