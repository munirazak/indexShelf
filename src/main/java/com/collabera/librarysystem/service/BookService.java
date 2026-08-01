package com.collabera.librarysystem.service;

import com.collabera.librarysystem.exception.DuplicateBookException;
import com.collabera.librarysystem.model.Book;
import com.collabera.librarysystem.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookFileReader bookFileReader;

    public BookService(BookRepository bookRepository, BookFileReader bookFileReader) {
        this.bookRepository = bookRepository;
        this.bookFileReader = bookFileReader;
    }

    public Book register(Book book) {
        if (bookRepository.existsById(book.getId())) {
            throw new DuplicateBookException(
                    "Book with id '" + book.getId() + "' already exists");
        }
        validateIsbnConsistency(book);
        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Transactional
    public List<Book> readBooksFromFile(MultipartFile file) {
        List<Book> booksFromFile = bookFileReader.read(file);
        List<Book> savedBooks = new ArrayList<>();
        for (Book book : booksFromFile) {
            savedBooks.add(register(book));
        }
        return savedBooks;
    }

    /**
     * Same ISBN is allowed only when title and author match existing books with that ISBN.
     */
    private void validateIsbnConsistency(Book book) {
        List<Book> existingWithIsbn = bookRepository.findByIsbn(book.getIsbn());
        for (Book existing : existingWithIsbn) {
            if (!existing.getTitle().equals(book.getTitle())
                    || !existing.getAuthor().equals(book.getAuthor())) {
                throw new DuplicateBookException(
                        "ISBN '" + book.getIsbn()
                                + "' already exists with a different title or author");
            }
        }
    }
}
