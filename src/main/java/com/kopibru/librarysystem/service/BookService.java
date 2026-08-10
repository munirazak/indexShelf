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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class BookService {

    private final BookCopyRepository bookCopyRepository;
    private final BorrowerRepository borrowerRepository;

    public BookService(
            BookCopyRepository bookCopyRepository,
            BorrowerRepository borrowerRepository) {
        this.bookCopyRepository = bookCopyRepository;
        this.borrowerRepository = borrowerRepository;
    }

    public List<BookDto> getBooks(String statusFilter) {
        List<BookCopy> copies = findCopiesByStatusFilter(statusFilter);
        return copies.stream()
                .map(copy -> toDto(copy, copy.getBookDetail(), false))
                .toList();
    }

    @Transactional
    public BookDto borrow(String libraryId, BorrowRequest request) {
        if (!borrowerRepository.existsByLibraryId(libraryId)) {
            throw new ResourceNotFoundException(
                    "Borrower with libraryId '" + libraryId + "' not found");
        }

        BookCopy copy = bookCopyRepository.findByIdWithDetailForUpdate(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book copy with id '" + request.getBookId() + "' not found"));

        if (copy.getStatus() != BookStatus.AVAILABLE) {
            throw new BookNotAvailableException(
                    "Book copy with id '" + request.getBookId() + "' is not available");
        }

        copy.setLibraryId(libraryId);
        copy.setStatus(BookStatus.BORROWED);
        BookCopy saved = bookCopyRepository.save(copy);
        return toDto(saved, saved.getBookDetail());
    }

    @Transactional
    public BookDto returnBook(String libraryId, BorrowRequest request) {
        if (!borrowerRepository.existsByLibraryId(libraryId)) {
            throw new ResourceNotFoundException(
                    "Borrower with libraryId '" + libraryId + "' not found");
        }

        BookCopy copy = bookCopyRepository.findByIdWithDetailForUpdate(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book copy with id '" + request.getBookId() + "' not found"));

        if (copy.getStatus() != BookStatus.BORROWED
                || !libraryId.equals(copy.getLibraryId())) {
            throw new BookReturnException(
                    "Book copy with id '" + request.getBookId()
                            + "' is not borrowed by libraryId '" + libraryId + "'");
        }

        copy.setLibraryId(null);
        copy.setStatus(BookStatus.AVAILABLE);
        BookCopy saved = bookCopyRepository.save(copy);
        return toDto(saved, saved.getBookDetail(), false);
    }

    private List<BookCopy> findCopiesByStatusFilter(String statusFilter) {
        String normalized = statusFilter == null ? "all" : statusFilter.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "all" -> bookCopyRepository.findAllWithDetail();
            case "available" -> bookCopyRepository.findAllWithDetailByStatus(BookStatus.AVAILABLE);
            case "borrowed" -> bookCopyRepository.findAllWithDetailByStatus(BookStatus.BORROWED);
            default -> throw new InvalidBookStatusException(
                    "Invalid status '" + statusFilter + "'. Allowed values: all, available, borrowed");
        };
    }

    private BookDto toDto(BookCopy copy, BookDetail detail) {
        return toDto(copy, detail, true);
    }

    private BookDto toDto(BookCopy copy, BookDetail detail, boolean includeLibraryId) {
        return new BookDto(
                copy.getId(),
                detail.getIsbn(),
                detail.getTitle(),
                detail.getAuthor(),
                copy.getStatus(),
                includeLibraryId ? copy.getLibraryId() : null);
    }
}
