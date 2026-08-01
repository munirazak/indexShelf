package com.collabera.librarysystem.service;

import com.collabera.librarysystem.exception.DuplicateBookException;
import com.collabera.librarysystem.model.Book;
import com.collabera.librarysystem.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book register(Book book) {
        if (bookRepository.existsById(book.getId())) {
            throw new DuplicateBookException(
                    "Book with id '" + book.getId() + "' already exists");
        }
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new DuplicateBookException(
                    "Book with ISBN '" + book.getIsbn() + "' already exists");
        }
        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
}
