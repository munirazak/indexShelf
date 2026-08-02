package com.collabera.librarysystem.controller;

import com.collabera.librarysystem.dto.BookDto;
import com.collabera.librarysystem.dto.BorrowRequest;
import com.collabera.librarysystem.exception.BookNotAvailableException;
import com.collabera.librarysystem.exception.GlobalExceptionHandler;
import com.collabera.librarysystem.exception.ResourceNotFoundException;
import com.collabera.librarysystem.model.BookStatus;
import com.collabera.librarysystem.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void registerBook_returnsCreated() throws Exception {
        BookDto book = new BookDto("BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null);
        when(bookService.register(any(BookDto.class))).thenReturn(book);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("BOOK-001"));
    }

    @Test
    void getBooks_returnsOk() throws Exception {
        when(bookService.getBooks("available")).thenReturn(List.of(
                new BookDto("BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null)));

        mockMvc.perform(get("/api/books").param("status", "available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void borrowBook_returnsOk() throws Exception {
        BorrowRequest request = new BorrowRequest("LIB-001", "BOOK-001");
        BookDto response = new BookDto(
                "BOOK-001", "978-1", "Title", "Author", BookStatus.BORROWED, "LIB-001");
        when(bookService.borrow(any(BorrowRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/books/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BORROWED"))
                .andExpect(jsonPath("$.libraryId").value("LIB-001"));
    }

    @Test
    void borrowBook_returnsNotFound() throws Exception {
        BorrowRequest request = new BorrowRequest("LIB-001", "BOOK-001");
        when(bookService.borrow(any(BorrowRequest.class)))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(post("/api/books/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void borrowBook_returnsConflictWhenUnavailable() throws Exception {
        BorrowRequest request = new BorrowRequest("LIB-001", "BOOK-001");
        when(bookService.borrow(any(BorrowRequest.class)))
                .thenThrow(new BookNotAvailableException("not available"));

        mockMvc.perform(post("/api/books/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void returnBook_returnsOk() throws Exception {
        BorrowRequest request = new BorrowRequest("LIB-001", "BOOK-001");
        BookDto response = new BookDto(
                "BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null);
        when(bookService.returnBook(any(BorrowRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/books/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void importFromFile_returnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.txt", "text/plain", "BOOK-001|978-1|Title|Author".getBytes());
        when(bookService.readBooksFromFile(any())).thenReturn(List.of(
                new BookDto("BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null)));

        mockMvc.perform(multipart("/api/books/from-file").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value("BOOK-001"));
    }

    @Test
    void registerBook_returnsBadRequestWhenInvalid() throws Exception {
        String body = """
                {"id":"","isbn":"","title":"","author":""}
                """;

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
