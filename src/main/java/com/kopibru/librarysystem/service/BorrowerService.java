package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.dto.BorrowerRegistrationRequest;
import com.kopibru.librarysystem.exception.DuplicateBorrowerException;
import com.kopibru.librarysystem.model.Borrower;
import com.kopibru.librarysystem.model.BorrowerCredentials;
import com.kopibru.librarysystem.repository.BorrowerCredentialsRepository;
import com.kopibru.librarysystem.repository.BorrowerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BorrowerService {

    private static final Logger log = LoggerFactory.getLogger(BorrowerService.class);

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
        log.info("Registering borrower libraryId={} username={}", request.getLibraryId(), request.getUsername());
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

        log.info("Borrower registered id={} libraryId={}", savedBorrower.getId(), savedBorrower.getLibraryId());
        return savedBorrower;
    }

    public List<Borrower> getAllBorrowers() {
        log.info("Fetching all borrowers");
        List<Borrower> borrowers = borrowerRepository.findAll();
        log.info("Fetched {} borrowers", borrowers.size());
        return borrowers;
    }
}
