package com.example.qltd.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.qltd.dto.request.RegisterRequest;
import com.example.qltd.dto.response.UserResponse;
import com.example.qltd.entity.User;
import com.example.qltd.enums.Role;
import com.example.qltd.enums.UserStatus;
import com.example.qltd.exception.BadRequestException;
import com.example.qltd.exception.DuplicateResourceException;
import com.example.qltd.repository.UserRepository;
import com.example.qltd.service.impl.AuthServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("Candidate@Gmail.com");
        request.setPassword("123456Aa");
        request.setConfirmPassword("123456Aa");
        request.setPhone("0909123456");
    }

    @Test
    void register_success() {
        // Arrange
        when(userRepository.existsByEmail("candidate@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456Aa")).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("candidate@gmail.com")
                .password("encodedPassword")
                .phone("0909123456")
                .role(Role.CANDIDATE)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(response.getEmail()).isEqualTo("candidate@gmail.com");
        assertThat(response.getRole()).isEqualTo(Role.CANDIDATE);
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).existsByEmail("candidate@gmail.com");
        verify(passwordEncoder).encode("123456Aa");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailAlreadyExists_shouldThrowDuplicateResourceException() {
        // Arrange
        when(userRepository.existsByEmail("candidate@gmail.com")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists");

        verify(userRepository).existsByEmail("candidate@gmail.com");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_confirmPasswordNotMatch_shouldThrowBadRequestException() {
        // Arrange
        request.setConfirmPassword("WrongPassword123");

        // Act + Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Password confirmation does not match");

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}

