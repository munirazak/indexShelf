package com.collabera.librarysystem.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("change-this-secret-key-to-at-least-32-chars", 3_600_000L);
    }

    @Test
    void generateAndExtractLibraryId() {
        String token = jwtService.generateToken("LIB-001", "alice");

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractLibraryId(token)).isEqualTo("LIB-001");
    }

    @Test
    void isTokenValid_returnsFalseForGarbage() {
        assertThat(jwtService.isTokenValid("not-a-token")).isFalse();
    }
}
