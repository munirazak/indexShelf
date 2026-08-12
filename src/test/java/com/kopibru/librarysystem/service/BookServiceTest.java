package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.client.BookManagementClient;
import com.kopibru.librarysystem.dto.BookDto;
import com.kopibru.librarysystem.dto.BorrowRequest;
import com.kopibru.librarysystem.exception.BookNotAvailableException;
import com.kopibru.librarysystem.exception.ResourceNotFoundException;
import com.kopibru.librarysystem.model.BookStatus;
import com.kopibru.librarysystem.repository.BorrowerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookManagementClient bookManagementClient;
    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void getBooks_delegatesToBookManagement() {
        when(bookManagementClient.getBooks("available")).thenReturn(List.of(
                new BookDto("BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null)));

        List<BookDto> result = bookService.getBooks("available");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("BOOK-001");
    }

    @Test
    void borrow_delegatesWhenBorrowerExists() {
        BorrowRequest request = new BorrowRequest("BOOK-001");
        BookDto response = new BookDto(
                "BOOK-001", "978-1", "Title", "Author", BookStatus.BORROWED, "LIB-001");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);
        when(bookManagementClient.borrow("BOOK-001", "LIB-001")).thenReturn(response);

        BookDto result = bookService.borrow("LIB-001", request);

        assertThat(result.getStatus()).isEqualTo(BookStatus.BORROWED);
        assertThat(result.getLibraryId()).isEqualTo("LIB-001");
    }

    @Test
    void borrow_throwsWhenBorrowerMissing() {
        BorrowRequest request = new BorrowRequest("BOOK-001");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(false);

        assertThatThrownBy(() -> bookService.borrow("LIB-001", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("LIB-001");
        verify(bookManagementClient, never()).borrow("BOOK-001", "LIB-001");
    }

    @Test
    void borrow_propagatesBookNotAvailable() {
        BorrowRequest request = new BorrowRequest("BOOK-001");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);
        when(bookManagementClient.borrow("BOOK-001", "LIB-001"))
                .thenThrow(new BookNotAvailableException("not available"));

        assertThatThrownBy(() -> bookService.borrow("LIB-001", request))
                .isInstanceOf(BookNotAvailableException.class);
    }

    @Test
    void returnBook_delegatesWhenBorrowerExists() {
        BorrowRequest request = new BorrowRequest("BOOK-001");
        BookDto response = new BookDto(
                "BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null);
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);
        when(bookManagementClient.returnBook("BOOK-001", "LIB-001")).thenReturn(response);

        BookDto result = bookService.returnBook("LIB-001", request);

        assertThat(result.getStatus()).isEqualTo(BookStatus.AVAILABLE);
    }

    @Test
    void returnBook_throwsWhenBorrowerMissing() {
        BorrowRequest request = new BorrowRequest("BOOK-001");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(false);

        assertThatThrownBy(() -> bookService.returnBook("LIB-001", request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(bookManagementClient, never()).returnBook("BOOK-001", "LIB-001");
    }
}
