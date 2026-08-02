package com.collabera.librarysystem.service;

import com.collabera.librarysystem.dto.BookDto;
import com.collabera.librarysystem.dto.BorrowRequest;
import com.collabera.librarysystem.exception.BookNotAvailableException;
import com.collabera.librarysystem.exception.DuplicateBookException;
import com.collabera.librarysystem.exception.InvalidBookStatusException;
import com.collabera.librarysystem.exception.ResourceNotFoundException;
import com.collabera.librarysystem.model.BookCopy;
import com.collabera.librarysystem.model.BookDetail;
import com.collabera.librarysystem.model.BookStatus;
import com.collabera.librarysystem.repository.BookCopyRepository;
import com.collabera.librarysystem.repository.BookDetailRepository;
import com.collabera.librarysystem.repository.BorrowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class BookService {

    private final BookDetailRepository bookDetailRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowerRepository borrowerRepository;
    private final BookFileReader bookFileReader;

    public BookService(
            BookDetailRepository bookDetailRepository,
            BookCopyRepository bookCopyRepository,
            BorrowerRepository borrowerRepository,
            BookFileReader bookFileReader) {
        this.bookDetailRepository = bookDetailRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.borrowerRepository = borrowerRepository;
        this.bookFileReader = bookFileReader;
    }

    @Transactional
    public BookDto register(BookDto book) {
        if (bookCopyRepository.existsById(book.getId())) {
            throw new DuplicateBookException(
                    "Book copy with id '" + book.getId() + "' already exists");
        }

        BookDetail detail = bookDetailRepository.findById(book.getIsbn())
                .map(existing -> validateAndReuseDetail(existing, book))
                .orElseGet(() -> bookDetailRepository.save(
                        new BookDetail(book.getIsbn(), book.getTitle(), book.getAuthor())));

        BookStatus status = book.getStatus() != null ? book.getStatus() : BookStatus.AVAILABLE;
        BookCopy copy = bookCopyRepository.save(new BookCopy(book.getId(), detail, status));
        return toDto(copy, detail);
    }

    public List<BookDto> getBooks(String statusFilter) {
        List<BookCopy> copies = findCopiesByStatusFilter(statusFilter);
        return copies.stream()
                .map(copy -> toDto(copy, copy.getBookDetail(), false))
                .toList();
    }

    @Transactional
    public BookDto borrow(BorrowRequest request) {
        if (!borrowerRepository.existsByLibraryId(request.getLibraryId())) {
            throw new ResourceNotFoundException(
                    "Borrower with libraryId '" + request.getLibraryId() + "' not found");
        }

        BookCopy copy = bookCopyRepository.findByIdWithDetail(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book copy with id '" + request.getBookId() + "' not found"));

        if (copy.getStatus() != BookStatus.AVAILABLE) {
            throw new BookNotAvailableException(
                    "Book copy with id '" + request.getBookId() + "' is not available");
        }

        copy.setLibraryId(request.getLibraryId());
        copy.setStatus(BookStatus.OCCUPIED);
        BookCopy saved = bookCopyRepository.save(copy);
        return toDto(saved, saved.getBookDetail());
    }

    @Transactional
    public List<BookDto> readBooksFromFile(MultipartFile file) {
        List<BookDto> booksFromFile = bookFileReader.read(file);
        List<BookDto> savedBooks = new ArrayList<>();
        for (BookDto book : booksFromFile) {
            savedBooks.add(register(book));
        }
        return savedBooks;
    }

    private List<BookCopy> findCopiesByStatusFilter(String statusFilter) {
        String normalized = statusFilter == null ? "all" : statusFilter.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "all" -> bookCopyRepository.findAllWithDetail();
            case "available" -> bookCopyRepository.findAllWithDetailByStatus(BookStatus.AVAILABLE);
            case "occupied" -> bookCopyRepository.findAllWithDetailByStatus(BookStatus.OCCUPIED);
            default -> throw new InvalidBookStatusException(
                    "Invalid status '" + statusFilter + "'. Allowed values: all, available, occupied");
        };
    }

    private BookDetail validateAndReuseDetail(BookDetail existing, BookDto book) {
        if (!existing.getTitle().equals(book.getTitle())
                || !existing.getAuthor().equals(book.getAuthor())) {
            throw new DuplicateBookException(
                    "ISBN '" + book.getIsbn()
                            + "' already exists with a different title or author");
        }
        return existing;
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
