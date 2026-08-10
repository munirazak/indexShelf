package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.dto.BookDto;
import com.kopibru.librarysystem.dto.BorrowRequest;
import com.kopibru.librarysystem.exception.BookNotAvailableException;
import com.kopibru.librarysystem.exception.BookReturnException;
import com.kopibru.librarysystem.exception.InvalidBookStatusException;
import com.kopibru.librarysystem.exception.ResourceNotFoundException;
import com.kopibru.librarysystem.model.BookCopy;
import com.kopibru.librarysystem.model.BookDetail;
import com.kopibru.librarysystem.model.BookStatus;
import com.kopibru.librarysystem.repository.BookCopyRepository;
import com.kopibru.librarysystem.repository.BorrowerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookCopyRepository bookCopyRepository;
    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void getBooks_returnsAllWhenStatusAll() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.BORROWED);
        copy.setLibraryId("LIB-001");
        when(bookCopyRepository.findAllWithDetail()).thenReturn(List.of(copy));

        List<BookDto> result = bookService.getBooks("all");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLibraryId()).isNull();
    }

    @Test
    void getBooks_filtersBorrowed() {
        when(bookCopyRepository.findAllWithDetailByStatus(BookStatus.BORROWED)).thenReturn(List.of());

        assertThat(bookService.getBooks("borrowed")).isEmpty();
    }

    @Test
    void getBooks_filtersByStatus() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.AVAILABLE);
        when(bookCopyRepository.findAllWithDetailByStatus(BookStatus.AVAILABLE))
                .thenReturn(List.of(copy));

        List<BookDto> result = bookService.getBooks("available");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLibraryId()).isNull();
    }

    @Test
    void getBooks_throwsForInvalidStatus() {
        assertThatThrownBy(() -> bookService.getBooks("missing"))
                .isInstanceOf(InvalidBookStatusException.class);
    }

    @Test
    void borrow_succeedsWhenAvailable() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.AVAILABLE);
        BorrowRequest request = new BorrowRequest("BOOK-001");

        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.of(copy));
        when(bookCopyRepository.save(copy)).thenAnswer(invocation -> invocation.getArgument(0));

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
    }

    @Test
    void borrow_throwsWhenBookMissing() {
        BorrowRequest request = new BorrowRequest("BOOK-001");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.borrow("LIB-001", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("BOOK-001");
    }

    @Test
    void borrow_throwsWhenNotAvailable() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.BORROWED);
        copy.setLibraryId("LIB-999");
        BorrowRequest request = new BorrowRequest("BOOK-001");

        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> bookService.borrow("LIB-001", request))
                .isInstanceOf(BookNotAvailableException.class);
    }

    @Test
    void returnBook_succeedsWhenBorrowedBySameBorrower() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.BORROWED);
        copy.setLibraryId("LIB-001");
        BorrowRequest request = new BorrowRequest("BOOK-001");

        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.of(copy));
        when(bookCopyRepository.save(copy)).thenAnswer(invocation -> invocation.getArgument(0));

        BookDto result = bookService.returnBook("LIB-001", request);

        assertThat(result.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(result.getLibraryId()).isNull();
    }

    @Test
    void returnBook_throwsWhenBorrowerMissing() {
        BorrowRequest request = new BorrowRequest("BOOK-001");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(false);

        assertThatThrownBy(() -> bookService.returnBook("LIB-001", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void returnBook_throwsWhenBookMissing() {
        BorrowRequest request = new BorrowRequest("BOOK-001");
        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.returnBook("LIB-001", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void returnBook_throwsWhenNotBorrowedByBorrower() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.AVAILABLE);
        BorrowRequest request = new BorrowRequest("BOOK-001");

        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> bookService.returnBook("LIB-001", request))
                .isInstanceOf(BookReturnException.class);
    }
}
