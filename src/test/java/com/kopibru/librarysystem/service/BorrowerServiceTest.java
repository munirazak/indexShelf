package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.dto.BorrowerRegistrationRequest;
import com.kopibru.librarysystem.exception.DuplicateBorrowerException;
import com.kopibru.librarysystem.model.Borrower;
import com.kopibru.librarysystem.model.BorrowerCredentials;
import com.kopibru.librarysystem.repository.BorrowerCredentialsRepository;
import com.kopibru.librarysystem.repository.BorrowerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BorrowerCredentialsRepository borrowerCredentialsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BorrowerService borrowerService;

    @Test
    void register_savesBorrowerAndHashedCredentials() {
        BorrowerRegistrationRequest request = new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123");
        Borrower savedBorrower = new Borrower("LIB-001", "Alice", "alice@example.com");
        savedBorrower.setId(1L);

        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(false);
        when(borrowerCredentialsRepository.existsByUsername("alice")).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(savedBorrower);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(borrowerCredentialsRepository.save(any(BorrowerCredentials.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Borrower result = borrowerService.register(request);

        assertThat(result.getLibraryId()).isEqualTo("LIB-001");
        verify(borrowerRepository).save(any(Borrower.class));

        ArgumentCaptor<BorrowerCredentials> credentialsCaptor =
                ArgumentCaptor.forClass(BorrowerCredentials.class);
        verify(borrowerCredentialsRepository).save(credentialsCaptor.capture());
        BorrowerCredentials credentials = credentialsCaptor.getValue();
        assertThat(credentials.getUsername()).isEqualTo("alice");
        assertThat(credentials.getPassword()).isEqualTo("hashed-password");
        assertThat(credentials.getBorrower()).isEqualTo(savedBorrower);
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_throwsWhenLibraryIdExists() {
        BorrowerRegistrationRequest request = new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);

        assertThatThrownBy(() -> borrowerService.register(request))
                .isInstanceOf(DuplicateBorrowerException.class)
                .hasMessageContaining("LIB-001");
        verify(borrowerRepository, never()).save(any());
        verify(borrowerCredentialsRepository, never()).save(any());
    }

    @Test
    void register_throwsWhenUsernameExists() {
        BorrowerRegistrationRequest request = new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(false);
        when(borrowerCredentialsRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> borrowerService.register(request))
                .isInstanceOf(DuplicateBorrowerException.class)
                .hasMessageContaining("alice");
        verify(borrowerRepository, never()).save(any());
        verify(borrowerCredentialsRepository, never()).save(any());
    }

    @Test
    void getAllBorrowers_returnsList() {
        when(borrowerRepository.findAll()).thenReturn(List.of(
                new Borrower("LIB-001", "Alice", "alice@example.com")));

        List<Borrower> result = borrowerService.getAllBorrowers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice");
    }
}
