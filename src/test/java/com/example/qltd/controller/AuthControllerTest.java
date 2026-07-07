package com.example.qltd.controller;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.qltd.dto.request.RegisterRequest;
import com.example.qltd.dto.response.UserResponse;
import com.example.qltd.enums.Role;
import com.example.qltd.enums.UserStatus;
import com.example.qltd.service.AuthService;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_success_shouldReturnCreated() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("candidate@gmail.com");
        request.setPassword("123456Aa");
        request.setConfirmPassword("123456Aa");
        request.setPhone("0909123456");

        UserResponse response = UserResponse.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("candidate@gmail.com")
                .phone("0909123456")
                .role(Role.CANDIDATE)
                .status(UserStatus.ACTIVE)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("candidate@gmail.com"))
                .andExpect(jsonPath("$.data.role").value("CANDIDATE"));
    }

    @Test
    void register_invalidRequest_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("");
        request.setEmail("abc");
        request.setPassword("123");
        request.setConfirmPassword("");
        request.setPhone("123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
