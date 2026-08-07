package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.dto.BorrowerRegistrationRequest;
import com.kopibru.librarysystem.exception.DuplicateBorrowerException;
import com.kopibru.librarysystem.model.Borrower;
import com.kopibru.librarysystem.model.BorrowerCredentials;
import com.kopibru.librarysystem.repository.BorrowerCredentialsRepository;
import com.kopibru.librarysystem.repository.BorrowerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final BorrowerCredentialsRepository borrowerCredentialsRepository;
    private final PasswordEncoder passwordEncoder;

    public BorrowerService(BorrowerRepository borrowerRepository,
                           BorrowerCredentialsRepository borrowerCredentialsRepository,
                           PasswordEncoder passwordEncoder) {
        this.borrowerRepository = borrowerRepository;
        this.borrowerCredentialsRepository = borrowerCredentialsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Borrower register(BorrowerRegistrationRequest request) {
        if (borrowerRepository.existsByLibraryId(request.getLibraryId())) {
            throw new DuplicateBorrowerException(
                    "Borrower with libraryId '" + request.getLibraryId() + "' already exists");
        }
        if (borrowerCredentialsRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateBorrowerException(
                    "Username '" + request.getUsername() + "' is already taken");
        }

        Borrower borrower = new Borrower(request.getLibraryId(), request.getName(), request.getEmail());
        Borrower savedBorrower = borrowerRepository.save(borrower);

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        BorrowerCredentials credentials = new BorrowerCredentials(
                savedBorrower, request.getUsername(), hashedPassword);
        borrowerCredentialsRepository.save(credentials);

        return savedBorrower;
    }

    public List<Borrower> getAllBorrowers() {
        return borrowerRepository.findAll();
    }
}
