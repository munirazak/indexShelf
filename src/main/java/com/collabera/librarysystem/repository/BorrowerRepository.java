package com.collabera.librarysystem.repository;

import com.collabera.librarysystem.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, String> {

    boolean existsByLibraryId(String libraryId);
}
