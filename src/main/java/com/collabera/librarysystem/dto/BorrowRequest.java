package com.collabera.librarysystem.dto;

import jakarta.validation.constraints.NotBlank;

public class BorrowRequest {

    @NotBlank(message = "bookId is required")
    private String bookId;

    public BorrowRequest() {
    }

    public BorrowRequest(String bookId) {
        this.bookId = bookId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}
