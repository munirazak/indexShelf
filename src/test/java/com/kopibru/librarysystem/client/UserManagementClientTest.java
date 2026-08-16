package com.kopibru.librarysystem.client;

import com.kopibru.librarysystem.dto.BorrowerRegistrationRequest;
import com.kopibru.librarysystem.dto.LoginRequest;
import com.kopibru.librarysystem.dto.LoginResponse;
import com.kopibru.librarysystem.exception.DuplicateBorrowerException;
import com.kopibru.librarysystem.exception.UnauthorizedException;
import com.kopibru.librarysystem.model.Borrower;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserManagementClientTest {

    private MockRestServiceServer server;
    private UserManagementClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8082");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new UserManagementClient(builder.build(), new ObjectMapper());
    }

    @Test
    void register_returnsBorrower() {
        server.expect(requestTo("http://localhost:8082/api/borrowers"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"id":1,"libraryId":"LIB-001","name":"Alice","email":"alice@example.com"}
                        """, MediaType.APPLICATION_JSON));

        Borrower result = client.register(new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123"));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLibraryId()).isEqualTo("LIB-001");
        server.verify();
    }

    @Test
    void register_throwsDuplicateOnConflict() {
        server.expect(requestTo("http://localhost:8082/api/borrowers"))
                .andRespond(withStatus(CONFLICT).body("""
                        {"error":"Borrower with libraryId 'LIB-001' already exists"}
                        """).contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.register(new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123")))
                .isInstanceOf(DuplicateBorrowerException.class)
                .hasMessageContaining("LIB-001");
        server.verify();
    }

    @Test
    void register_throwsIllegalArgumentOnBadRequest() {
        server.expect(requestTo("http://localhost:8082/api/borrowers"))
                .andRespond(withBadRequest().body("""
                        {"error":"Validation failed"}
                        """).contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.register(new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123")))
                .isInstanceOf(IllegalArgumentException.class);
        server.verify();
    }

    @Test
    void register_throwsIllegalStateOnServerError() {
        server.expect(requestTo("http://localhost:8082/api/borrowers"))
                .andRespond(withServerError().body("boom"));

        assertThatThrownBy(() -> client.register(new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UserManagement error");
        server.verify();
    }

    @Test
    void login_returnsToken() {
        server.expect(requestTo("http://localhost:8082/api/auth/login"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"token":"jwt-token","libraryId":"LIB-001","username":"alice"}
                        """, MediaType.APPLICATION_JSON));

        LoginResponse response = client.login(new LoginRequest("alice", "password123"));

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getLibraryId()).isEqualTo("LIB-001");
        server.verify();
    }

    @Test
    void login_throwsUnauthorized() {
        server.expect(requestTo("http://localhost:8082/api/auth/login"))
                .andRespond(withStatus(UNAUTHORIZED).body("""
                        {"error":"Invalid username or password"}
                        """).contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.login(new LoginRequest("alice", "wrong")))
                .isInstanceOf(UnauthorizedException.class);
        server.verify();
    }

    @Test
    void login_throwsIllegalArgumentOnBadRequest() {
        server.expect(requestTo("http://localhost:8082/api/auth/login"))
                .andRespond(withBadRequest().body("""
                        {"error":"Validation failed"}
                        """).contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.login(new LoginRequest("alice", "password123")))
                .isInstanceOf(IllegalArgumentException.class);
        server.verify();
    }

    @Test
    void login_throwsIllegalStateOnServerError() {
        server.expect(requestTo("http://localhost:8082/api/auth/login"))
                .andRespond(withServerError().body("boom"));

        assertThatThrownBy(() -> client.login(new LoginRequest("alice", "password123")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UserManagement error");
        server.verify();
    }

    @Test
    void existsByLibraryId_returnsTrueWhenFound() {
        server.expect(requestTo("http://localhost:8082/api/borrowers/LIB-001"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":1,"libraryId":"LIB-001","name":"Alice","email":"alice@example.com"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.existsByLibraryId("LIB-001")).isTrue();
        server.verify();
    }

    @Test
    void existsByLibraryId_returnsFalseWhenMissing() {
        server.expect(requestTo("http://localhost:8082/api/borrowers/LIB-999"))
                .andRespond(withStatus(NOT_FOUND).body("""
                        {"error":"Borrower with libraryId 'LIB-999' not found"}
                        """).contentType(MediaType.APPLICATION_JSON));

        assertThat(client.existsByLibraryId("LIB-999")).isFalse();
        server.verify();
    }

    @Test
    void existsByLibraryId_throwsIllegalStateOnServerError() {
        server.expect(requestTo("http://localhost:8082/api/borrowers/LIB-001"))
                .andRespond(withServerError().body("boom"));

        assertThatThrownBy(() -> client.existsByLibraryId("LIB-001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UserManagement error");
        server.verify();
    }
}
