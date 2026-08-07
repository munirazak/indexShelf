package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.dto.BookDto;
import com.kopibru.librarysystem.dto.BorrowRequest;
import com.kopibru.librarysystem.exception.BookNotAvailableException;
import com.kopibru.librarysystem.exception.BookReturnException;
import com.kopibru.librarysystem.exception.DuplicateBookException;
import com.kopibru.librarysystem.exception.InvalidBookStatusException;
import com.kopibru.librarysystem.exception.ResourceNotFoundException;
import com.kopibru.librarysystem.model.BookCopy;
import com.kopibru.librarysystem.model.BookDetail;
import com.kopibru.librarysystem.model.BookStatus;
import com.kopibru.librarysystem.repository.BookCopyRepository;
import com.kopibru.librarysystem.repository.BookDetailRepository;
import com.kopibru.librarysystem.repository.BorrowerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookDetailRepository bookDetailRepository;
    @Mock
    private BookCopyRepository bookCopyRepository;
    @Mock
    private BorrowerRepository borrowerRepository;
    @Mock
    private BookFileReader bookFileReader;

    @InjectMocks
    private BookService bookService;

    @Test
    void register_createsDetailAndCopy() {
        BookDto request = new BookDto("BOOK-001", "978-1", "Title", "Author", null, null);
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.AVAILABLE);

        when(bookCopyRepository.existsById("BOOK-001")).thenReturn(false);
        when(bookDetailRepository.findById("978-1")).thenReturn(Optional.empty());
        when(bookDetailRepository.save(any(BookDetail.class))).thenReturn(detail);
        when(bookCopyRepository.save(any(BookCopy.class))).thenReturn(copy);

        BookDto result = bookService.register(request);

        assertThat(result.getId()).isEqualTo("BOOK-001");
        assertThat(result.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        verify(bookDetailRepository).save(any(BookDetail.class));
    }

    @Test
    void register_reusesDetailWhenIsbnMatches() {
        BookDto request = new BookDto("BOOK-002", "978-1", "Title", "Author", null, null);
        BookDetail existing = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-002", existing, BookStatus.AVAILABLE);

        when(bookCopyRepository.existsById("BOOK-002")).thenReturn(false);
        when(bookDetailRepository.findById("978-1")).thenReturn(Optional.of(existing));
        when(bookCopyRepository.save(any(BookCopy.class))).thenReturn(copy);

        BookDto result = bookService.register(request);

        assertThat(result.getId()).isEqualTo("BOOK-002");
        verify(bookDetailRepository, never()).save(any());
    }

    @Test
    void register_throwsWhenCopyIdExists() {
        BookDto request = new BookDto("BOOK-001", "978-1", "Title", "Author", null, null);
        when(bookCopyRepository.existsById("BOOK-001")).thenReturn(true);

        assertThatThrownBy(() -> bookService.register(request))
                .isInstanceOf(DuplicateBookException.class)
                .hasMessageContaining("BOOK-001");
    }

    @Test
    void register_throwsWhenIsbnHasDifferentTitleOrAuthor() {
        BookDto request = new BookDto("BOOK-002", "978-1", "Other Title", "Author", null, null);
        BookDetail existing = new BookDetail("978-1", "Title", "Author");

        when(bookCopyRepository.existsById("BOOK-002")).thenReturn(false);
        when(bookDetailRepository.findById("978-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookService.register(request))
                .isInstanceOf(DuplicateBookException.class)
                .hasMessageContaining("978-1");
    }

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
    void returnBook_throwsWhenNotBorrowedByBorrower() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.AVAILABLE);
        BorrowRequest request = new BorrowRequest("BOOK-001");

        when(borrowerRepository.existsByLibraryId("LIB-001")).thenReturn(true);
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> bookService.returnBook("LIB-001", request))
                .isInstanceOf(BookReturnException.class);
    }

    @Test
    void readBooksFromFile_registersParsedBooks() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.txt", "text/plain", "data".getBytes());
        BookDto parsed = new BookDto("BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null);
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.AVAILABLE);

        when(bookFileReader.read(file)).thenReturn(List.of(parsed));
        when(bookCopyRepository.existsById("BOOK-001")).thenReturn(false);
        when(bookDetailRepository.findById("978-1")).thenReturn(Optional.empty());
        when(bookDetailRepository.save(any(BookDetail.class))).thenReturn(detail);
        when(bookCopyRepository.save(any(BookCopy.class))).thenReturn(copy);

        List<BookDto> result = bookService.readBooksFromFile(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("BOOK-001");
    }
}
