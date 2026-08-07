package com.kopibru.librarysystem.exception;

public class InvalidBookFileException extends RuntimeException {

    public InvalidBookFileException(String message) {
        super(message);
    }

    public InvalidBookFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
