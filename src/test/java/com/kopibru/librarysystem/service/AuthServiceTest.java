package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.client.UserManagementClient;
import com.kopibru.librarysystem.dto.LoginRequest;
import com.kopibru.librarysystem.dto.LoginResponse;
import com.kopibru.librarysystem.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserManagementClient userManagementClient;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_delegatesToUserManagement() {
        LoginRequest request = new LoginRequest("alice", "password123");
        when(userManagementClient.login(request))
                .thenReturn(new LoginResponse("jwt-token", "LIB-001", "alice"));

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getLibraryId()).isEqualTo("LIB-001");
        assertThat(response.getUsername()).isEqualTo("alice");
        verify(userManagementClient).login(request);
    }

    @Test
    void login_propagatesUnauthorized() {
        LoginRequest request = new LoginRequest("alice", "wrong");
        when(userManagementClient.login(request))
                .thenThrow(new UnauthorizedException("Invalid username or password"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }
}
