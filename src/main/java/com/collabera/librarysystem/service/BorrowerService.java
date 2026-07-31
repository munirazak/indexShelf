package com.collabera.librarysystem.service;

import com.collabera.librarysystem.exception.DuplicateBorrowerException;
import com.collabera.librarysystem.model.Borrower;
import com.collabera.librarysystem.repository.BorrowerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;

    public BorrowerService(BorrowerRepository borrowerRepository) {
        this.borrowerRepository = borrowerRepository;
    }

    public Borrower register(Borrower borrower) {
        if (borrowerRepository.existsByLibraryId(borrower.getLibraryId())) {
            throw new DuplicateBorrowerException(
                    "Borrower with libraryId '" + borrower.getLibraryId() + "' already exists");
        }
        return borrowerRepository.save(borrower);
    }

    public List<Borrower> getAllBorrowers() {
        return borrowerRepository.findAll();
    }
}
