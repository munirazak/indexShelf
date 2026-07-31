package com.collabera.librarysystem.repository;

import com.collabera.librarysystem.model.Borrower;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BorrowerRepository {

    private final Map<String, Borrower> borrowers = new ConcurrentHashMap<>();

    public Borrower save(Borrower borrower) {
        borrowers.put(borrower.getLibraryId(), borrower);
        return borrower;
    }

    public Optional<Borrower> findByLibraryId(String libraryId) {
        return Optional.ofNullable(borrowers.get(libraryId));
    }

    public boolean existsByLibraryId(String libraryId) {
        return borrowers.containsKey(libraryId);
    }

    public List<Borrower> findAll() {
        return new ArrayList<>(borrowers.values());
    }
}
