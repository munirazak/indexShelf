package com.kopibru.librarysystem.client;

import com.kopibru.librarysystem.dto.BorrowerRegistrationRequest;
import com.kopibru.librarysystem.dto.LoginRequest;
import com.kopibru.librarysystem.dto.LoginResponse;
import com.kopibru.librarysystem.exception.DuplicateBorrowerException;
import com.kopibru.librarysystem.exception.ResourceNotFoundException;
import com.kopibru.librarysystem.exception.UnauthorizedException;
import com.kopibru.librarysystem.model.Borrower;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserManagementClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public UserManagementClient(RestClient userManagementRestClient, ObjectMapper objectMapper) {
        this.restClient = userManagementRestClient;
        this.objectMapper = objectMapper;
    }

    public Borrower register(BorrowerRegistrationRequest request) {
        return restClient.post()
                .uri("/api/borrowers")
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, response) -> {
                    throw mapRegisterError(response.getStatusCode().value(), readBody(response));
                })
                .body(Borrower.class);
    }

    public LoginResponse login(LoginRequest request) {
        return restClient.post()
                .uri("/api/auth/login")
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, response) -> {
                    throw mapLoginError(response.getStatusCode().value(), readBody(response));
                })
                .body(LoginResponse.class);
    }

    public boolean existsByLibraryId(String libraryId) {
        try {
            restClient.get()
                    .uri("/api/borrowers/{libraryId}", libraryId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, response) -> {
                        throw mapLookupError(response.getStatusCode().value(), readBody(response));
                    })
                    .toBodilessEntity();
            return true;
        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }

    private RuntimeException mapRegisterError(int status, String body) {
        String message = extractErrorMessage(body);
        return switch (status) {
            case 409 -> new DuplicateBorrowerException(message);
            case 400 -> new IllegalArgumentException(message);
            default -> new IllegalStateException("UserManagement error (" + status + "): " + message);
        };
    }

    private RuntimeException mapLoginError(int status, String body) {
        String message = extractErrorMessage(body);
        return switch (status) {
            case 401 -> new UnauthorizedException(message);
            case 400 -> new IllegalArgumentException(message);
            default -> new IllegalStateException("UserManagement error (" + status + "): " + message);
        };
    }

    private RuntimeException mapLookupError(int status, String body) {
        String message = extractErrorMessage(body);
        return switch (status) {
            case 404 -> new ResourceNotFoundException(message);
            default -> new IllegalStateException("UserManagement error (" + status + "): " + message);
        };
    }

    private String readBody(org.springframework.http.client.ClientHttpResponse response) {
        try {
            return new String(response.getBody().readAllBytes());
        } catch (Exception ex) {
            return "";
        }
    }

    private String extractErrorMessage(String body) {
        if (body == null || body.isBlank()) {
            return "UserManagement request failed";
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.hasNonNull("error")) {
                return node.get("error").asText();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return body;
    }
}
