package com.kopibru.librarysystem.controller;

import com.kopibru.librarysystem.dto.LoginRequest;
import com.kopibru.librarysystem.dto.LoginResponse;
import com.kopibru.librarysystem.exception.GlobalExceptionHandler;
import com.kopibru.librarysystem.exception.UnauthorizedException;
import com.kopibru.librarysystem.security.JwtService;
import com.kopibru.librarysystem.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void login_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest("alice", "password123");
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt-token", "LIB-001", "alice"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.libraryId").value("LIB-001"))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void login_returnsUnauthorizedWhenInvalid() throws Exception {
        LoginRequest request = new LoginRequest("alice", "wrong");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returnsBadRequestWhenInvalidBody() throws Exception {
        String body = """
                {"username":"","password":""}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
