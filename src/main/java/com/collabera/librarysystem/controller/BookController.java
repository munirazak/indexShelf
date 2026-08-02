package com.collabera.librarysystem.controller;

import com.collabera.librarysystem.dto.BookDto;
import com.collabera.librarysystem.dto.BorrowRequest;
import com.collabera.librarysystem.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookDto> registerBook(@Valid @RequestBody BookDto book) {
        BookDto registered = bookService.register(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getBooks(
            @RequestParam(defaultValue = "all") String status) {
        return ResponseEntity.ok(bookService.getBooks(status));
    }

    @PostMapping("/borrow")
    public ResponseEntity<BookDto> borrowBook(@Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.ok(bookService.borrow(request));
    }

    @PostMapping(value = "/from-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<BookDto>> readBooksFromFile(@RequestParam("file") MultipartFile file) {
        List<BookDto> savedBooks = bookService.readBooksFromFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBooks);
    }
}
