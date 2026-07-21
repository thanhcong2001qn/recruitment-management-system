package com.example.qltd.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.qltd.auth.dto.request.LoginRequest;
import com.example.qltd.auth.dto.request.RegisterRequest;
import com.example.qltd.auth.dto.response.AuthResponse;
import com.example.qltd.auth.service.impl.AuthServiceImpl;
import com.example.qltd.common.exception.BadRequestException;
import com.example.qltd.common.exception.DuplicateResourceException;
import com.example.qltd.common.exception.UnauthorizedException;
import com.example.qltd.common.security.JwtService;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.shared.enums.UserStatus;
import com.example.qltd.user.dto.respone.UserResponse;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

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

    @Test
    void login_success_shouldReturnAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("Candidate@Gmail.com");
        request.setPassword("123456Aa");

        User user = User.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("candidate@gmail.com")
                .password("encodedPassword")
                .phone("0909123456")
                .role(Role.CANDIDATE)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("candidate@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456Aa", "encodedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("mock-jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(response.getEmail()).isEqualTo("candidate@gmail.com");
        assertThat(response.getRole()).isEqualTo(Role.CANDIDATE);

        verify(userRepository).findByEmail("candidate@gmail.com");
        verify(passwordEncoder).matches("123456Aa", "encodedPassword");
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_emailNotFound_shouldThrowUnauthorizedException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@gmail.com");
        request.setPassword("123456Aa");

        when(userRepository.findByEmail("notfound@gmail.com"))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail("notfound@gmail.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void login_wrongPassword_shouldThrowUnauthorizedException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("candidate@gmail.com");
        request.setPassword("WrongPassword123");

        User user = User.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("candidate@gmail.com")
                .password("encodedPassword")
                .role(Role.CANDIDATE)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("candidate@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("WrongPassword123", "encodedPassword"))
                .thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail("candidate@gmail.com");
        verify(passwordEncoder).matches("WrongPassword123", "encodedPassword");
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void login_inactiveUser_shouldThrowUnauthorizedException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("candidate@gmail.com");
        request.setPassword("123456Aa");

        User user = User.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("candidate@gmail.com")
                .password("encodedPassword")
                .role(Role.CANDIDATE)
                .status(UserStatus.INACTIVE)
                .build();

        when(userRepository.findByEmail("candidate@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456Aa", "encodedPassword"))
                .thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User account is not active");

        verify(userRepository).findByEmail("candidate@gmail.com");
        verify(passwordEncoder).matches("123456Aa", "encodedPassword");
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void login_bannedUser_shouldThrowUnauthorizedException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("candidate@gmail.com");
        request.setPassword("123456Aa");

        User user = User.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("candidate@gmail.com")
                .password("encodedPassword")
                .role(Role.CANDIDATE)
                .status(UserStatus.BANNED)
                .build();

        when(userRepository.findByEmail("candidate@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456Aa", "encodedPassword"))
                .thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User account is not active");

        verify(userRepository).findByEmail("candidate@gmail.com");
        verify(passwordEncoder).matches("123456Aa", "encodedPassword");
        verify(jwtService, never()).generateToken(any(User.class));
    }
}

