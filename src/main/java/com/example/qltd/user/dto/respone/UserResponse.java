package com.example.qltd.user.dto.respone;

import com.example.qltd.shared.enums.Role;
import com.example.qltd.shared.enums.UserStatus;

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
