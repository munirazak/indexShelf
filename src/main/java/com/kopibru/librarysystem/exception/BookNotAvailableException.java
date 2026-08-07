package com.kopibru.librarysystem.exception;

public class BookNotAvailableException extends RuntimeException {

    public BookNotAvailableException(String message) {
        super(message);
    }
}
