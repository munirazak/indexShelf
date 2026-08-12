package com.kopibru.librarysystem.client;

import com.kopibru.librarysystem.dto.BookDto;
import com.kopibru.librarysystem.exception.BookNotAvailableException;
import com.kopibru.librarysystem.exception.BookReturnException;
import com.kopibru.librarysystem.exception.InvalidBookStatusException;
import com.kopibru.librarysystem.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class BookManagementClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public BookManagementClient(RestClient bookManagementRestClient, ObjectMapper objectMapper) {
        this.restClient = bookManagementRestClient;
        this.objectMapper = objectMapper;
    }

    public List<BookDto> getBooks(String status) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/books")
                        .queryParam("status", status)
                        .queryParam("includeLibraryId", false)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw mapError(response.getStatusCode().value(), readBody(response));
                })
                .body(new ParameterizedTypeReference<List<BookDto>>() {
                });
    }

    public BookDto borrow(String bookId, String libraryId) {
        return restClient.post()
                .uri("/api/books/{bookId}/borrow", bookId)
                .body(Map.of("libraryId", libraryId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw mapBorrowError(response.getStatusCode().value(), readBody(response));
                })
                .body(BookDto.class);
    }

    public BookDto returnBook(String bookId, String libraryId) {
        return restClient.post()
                .uri("/api/books/{bookId}/return", bookId)
                .body(Map.of("libraryId", libraryId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw mapReturnError(response.getStatusCode().value(), readBody(response));
                })
                .body(BookDto.class);
    }

    private RuntimeException mapBorrowError(int status, String body) {
        String message = extractErrorMessage(body);
        return switch (status) {
            case 404 -> new ResourceNotFoundException(message);
            case 409 -> new BookNotAvailableException(message);
            case 400 -> new InvalidBookStatusException(message);
            default -> new IllegalStateException("BookManagement error (" + status + "): " + message);
        };
    }

    private RuntimeException mapReturnError(int status, String body) {
        String message = extractErrorMessage(body);
        return switch (status) {
            case 404 -> new ResourceNotFoundException(message);
            case 409 -> new BookReturnException(message);
            case 400 -> new InvalidBookStatusException(message);
            default -> new IllegalStateException("BookManagement error (" + status + "): " + message);
        };
    }

    private RuntimeException mapError(int status, String body) {
        String message = extractErrorMessage(body);
        return switch (status) {
            case 400 -> new InvalidBookStatusException(message);
            case 404 -> new ResourceNotFoundException(message);
            default -> new IllegalStateException("BookManagement error (" + status + "): " + message);
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
            return "BookManagement request failed";
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
