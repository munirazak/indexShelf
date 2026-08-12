package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.client.BookManagementClient;
import com.kopibru.librarysystem.dto.BookDto;
import com.kopibru.librarysystem.dto.BorrowRequest;
import com.kopibru.librarysystem.exception.ResourceNotFoundException;
import com.kopibru.librarysystem.repository.BorrowerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookManagementClient bookManagementClient;
    private final BorrowerRepository borrowerRepository;

    public BookService(
            BookManagementClient bookManagementClient,
            BorrowerRepository borrowerRepository) {
        this.bookManagementClient = bookManagementClient;
        this.borrowerRepository = borrowerRepository;
    }

    public List<BookDto> getBooks(String statusFilter) {
        return bookManagementClient.getBooks(statusFilter);
    }

    public BookDto borrow(String libraryId, BorrowRequest request) {
        ensureBorrowerExists(libraryId);
        return bookManagementClient.borrow(request.getBookId(), libraryId);
    }

    public BookDto returnBook(String libraryId, BorrowRequest request) {
        ensureBorrowerExists(libraryId);
        return bookManagementClient.returnBook(request.getBookId(), libraryId);
    }

    private void ensureBorrowerExists(String libraryId) {
        if (!borrowerRepository.existsByLibraryId(libraryId)) {
            throw new ResourceNotFoundException(
                    "Borrower with libraryId '" + libraryId + "' not found");
        }
    }
}
