package com.collabera.librarysystem.service;

import com.collabera.librarysystem.exception.DuplicateBorrowerException;
import com.collabera.librarysystem.model.Borrower;
import com.collabera.librarysystem.repository.BorrowerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private BorrowerService borrowerService;

    @Test
    void register_savesNewBorrower() {
        Borrower borrower = new Borrower("LIB-001", "Alice", "alice@example.com");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(false);
        when(borrowerRepository.save(borrower)).thenReturn(borrower);

        Borrower result = borrowerService.register(borrower);

        assertThat(result.getLibraryId()).isEqualTo("LIB-001");
        verify(borrowerRepository).save(borrower);
    }

    @Test
    void register_throwsWhenLibraryIdExists() {
        Borrower borrower = new Borrower("LIB-001", "Alice", "alice@example.com");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);

        assertThatThrownBy(() -> borrowerService.register(borrower))
                .isInstanceOf(DuplicateBorrowerException.class)
                .hasMessageContaining("LIB-001");
        verify(borrowerRepository, never()).save(any());
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
