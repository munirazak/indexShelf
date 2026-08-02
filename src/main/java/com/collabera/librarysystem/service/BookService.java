package com.collabera.librarysystem.service;

import com.collabera.librarysystem.dto.BookDto;
import com.collabera.librarysystem.exception.DuplicateBookException;
import com.collabera.librarysystem.model.BookCopy;
import com.collabera.librarysystem.model.BookDetail;
import com.collabera.librarysystem.model.BookStatus;
import com.collabera.librarysystem.repository.BookCopyRepository;
import com.collabera.librarysystem.repository.BookDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    private final BookDetailRepository bookDetailRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookFileReader bookFileReader;

    public BookService(
            BookDetailRepository bookDetailRepository,
            BookCopyRepository bookCopyRepository,
            BookFileReader bookFileReader) {
        this.bookDetailRepository = bookDetailRepository;
        this.bookCopyRepository = bookCopyRepository;
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

    public List<BookDto> getAllBooks() {
        return bookCopyRepository.findAllWithDetail().stream()
                .map(copy -> toDto(copy, copy.getBookDetail()))
                .toList();
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
        return new BookDto(
                copy.getId(),
                detail.getIsbn(),
                detail.getTitle(),
                detail.getAuthor(),
                copy.getStatus());
    }
}
