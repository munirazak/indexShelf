package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.dto.LoginRequest;
import com.kopibru.librarysystem.dto.LoginResponse;
import com.kopibru.librarysystem.exception.UnauthorizedException;
import com.kopibru.librarysystem.model.Borrower;
import com.kopibru.librarysystem.model.BorrowerCredentials;
import com.kopibru.librarysystem.repository.BorrowerCredentialsRepository;
import com.kopibru.librarysystem.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private BorrowerCredentialsRepository borrowerCredentialsRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_returnsTokenWhenCredentialsValid() {
        Borrower borrower = new Borrower("LIB-001", "Alice", "alice@example.com");
        BorrowerCredentials credentials = new BorrowerCredentials(borrower, "alice", "hashed");
        LoginRequest request = new LoginRequest("alice", "password123");

        when(borrowerCredentialsRepository.findByUsernameWithBorrower("alice"))
                .thenReturn(Optional.of(credentials));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken("LIB-001", "alice")).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getLibraryId()).isEqualTo("LIB-001");
        assertThat(response.getUsername()).isEqualTo("alice");
        verify(jwtService).generateToken("LIB-001", "alice");
    }

    @Test
    void login_throwsWhenUsernameUnknown() {
        when(borrowerCredentialsRepository.findByUsernameWithBorrower("alice"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "password123")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void login_throwsWhenPasswordInvalid() {
        Borrower borrower = new Borrower("LIB-001", "Alice", "alice@example.com");
        BorrowerCredentials credentials = new BorrowerCredentials(borrower, "alice", "hashed");

        when(borrowerCredentialsRepository.findByUsernameWithBorrower("alice"))
                .thenReturn(Optional.of(credentials));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "wrong")))
                .isInstanceOf(UnauthorizedException.class);
    }
}
