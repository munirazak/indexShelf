package com.kopibru.librarysystem.controller;

import com.kopibru.librarysystem.dto.BookDto;
import com.kopibru.librarysystem.dto.BorrowRequest;
import com.kopibru.librarysystem.security.AuthenticationUtils;
import com.kopibru.librarysystem.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getBooks(
            @RequestParam(defaultValue = "all") String status) {
        return ResponseEntity.ok(bookService.getBooks(status));
    }

    @PostMapping("/borrow")
    public ResponseEntity<BookDto> borrowBook(@Valid @RequestBody BorrowRequest request) {
        String libraryId = AuthenticationUtils.requireLibraryId();
        return ResponseEntity.ok(bookService.borrow(libraryId, request));
    }

    @PostMapping("/return")
    public ResponseEntity<BookDto> returnBook(@Valid @RequestBody BorrowRequest request) {
        String libraryId = AuthenticationUtils.requireLibraryId();
        return ResponseEntity.ok(bookService.returnBook(libraryId, request));
    }
}
