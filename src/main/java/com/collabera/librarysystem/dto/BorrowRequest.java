package com.collabera.librarysystem.dto;

import jakarta.validation.constraints.NotBlank;

public class BorrowRequest {

    @NotBlank(message = "libraryId is required")
    private String libraryId;

    @NotBlank(message = "bookId is required")
    private String bookId;

    public BorrowRequest() {
    }

    public BorrowRequest(String libraryId, String bookId) {
        this.libraryId = libraryId;
        this.bookId = bookId;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}
