package com.kopibru.librarysystem.repository;

import com.kopibru.librarysystem.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    boolean existsByLibraryId(String libraryId);
}
