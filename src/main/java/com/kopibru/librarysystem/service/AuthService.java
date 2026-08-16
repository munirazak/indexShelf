package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.client.UserManagementClient;
import com.kopibru.librarysystem.dto.LoginRequest;
import com.kopibru.librarysystem.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserManagementClient userManagementClient;

    public AuthService(UserManagementClient userManagementClient) {
        this.userManagementClient = userManagementClient;
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Authenticating via UserManagement username={}", request.getUsername());
        LoginResponse response = userManagementClient.login(request);
        log.info("Login succeeded username={} libraryId={}", response.getUsername(), response.getLibraryId());
        return response;
    }
}
