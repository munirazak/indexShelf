package com.collabera.librarysystem.repository;

import com.collabera.librarysystem.model.BorrowerCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowerCredentialsRepository extends JpaRepository<BorrowerCredentials, Long> {

    boolean existsByUsername(String username);

    Optional<BorrowerCredentials> findByUsername(String username);

    @Query("SELECT c FROM BorrowerCredentials c JOIN FETCH c.borrower WHERE c.username = :username")
    Optional<BorrowerCredentials> findByUsernameWithBorrower(@Param("username") String username);

    Optional<BorrowerCredentials> findByBorrowerLibraryId(String libraryId);
}
