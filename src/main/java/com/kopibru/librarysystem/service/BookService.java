package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.client.BookManagementClient;
import com.kopibru.librarysystem.client.UserManagementClient;
import com.kopibru.librarysystem.dto.BookDto;
import com.kopibru.librarysystem.dto.BorrowRequest;
import com.kopibru.librarysystem.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookManagementClient bookManagementClient;
    private final UserManagementClient userManagementClient;

    public BookService(
            BookManagementClient bookManagementClient,
            UserManagementClient userManagementClient) {
        this.bookManagementClient = bookManagementClient;
        this.userManagementClient = userManagementClient;
    }

    public List<BookDto> getBooks(String statusFilter) {
        log.info("Requesting books from BookManagement status={}", statusFilter);
        List<BookDto> books = bookManagementClient.getBooks(statusFilter);
        log.info("Received {} books from BookManagement status={}", books.size(), statusFilter);
        return books;
    }

    public BookDto borrow(String libraryId, BorrowRequest request) {
        log.info("Borrowing bookId={} for libraryId={}", request.getBookId(), libraryId);
        ensureBorrowerExists(libraryId);
        BookDto result = bookManagementClient.borrow(request.getBookId(), libraryId);
        log.info("Borrow succeeded bookId={} libraryId={} status={}", result.getId(), libraryId, result.getStatus());
        return result;
    }

    public BookDto returnBook(String libraryId, BorrowRequest request) {
        log.info("Returning bookId={} for libraryId={}", request.getBookId(), libraryId);
        ensureBorrowerExists(libraryId);
        BookDto result = bookManagementClient.returnBook(request.getBookId(), libraryId);
        log.info("Return succeeded bookId={} libraryId={} status={}", result.getId(), libraryId, result.getStatus());
        return result;
    }

    private void ensureBorrowerExists(String libraryId) {
        if (!userManagementClient.existsByLibraryId(libraryId)) {
            throw new ResourceNotFoundException(
                    "Borrower with libraryId '" + libraryId + "' not found");
        }
    }
}
