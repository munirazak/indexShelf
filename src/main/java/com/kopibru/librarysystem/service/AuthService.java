package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.dto.LoginRequest;
import com.kopibru.librarysystem.dto.LoginResponse;
import com.kopibru.librarysystem.exception.UnauthorizedException;
import com.kopibru.librarysystem.model.BorrowerCredentials;
import com.kopibru.librarysystem.repository.BorrowerCredentialsRepository;
import com.kopibru.librarysystem.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final BorrowerCredentialsRepository borrowerCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            BorrowerCredentialsRepository borrowerCredentialsRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.borrowerCredentialsRepository = borrowerCredentialsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        BorrowerCredentials credentials = borrowerCredentialsRepository
                .findByUsernameWithBorrower(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), credentials.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String libraryId = credentials.getBorrower().getLibraryId();
        String token = jwtService.generateToken(libraryId, credentials.getUsername());
        return new LoginResponse(token, libraryId, credentials.getUsername());
    }
}
