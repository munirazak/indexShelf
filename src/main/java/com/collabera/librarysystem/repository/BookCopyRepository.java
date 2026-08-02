package com.collabera.librarysystem.repository;

import com.collabera.librarysystem.model.BookCopy;
import com.collabera.librarysystem.model.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, String> {

    @Query("SELECT c FROM BookCopy c JOIN FETCH c.bookDetail")
    List<BookCopy> findAllWithDetail();

    @Query("SELECT c FROM BookCopy c JOIN FETCH c.bookDetail WHERE c.status = :status")
    List<BookCopy> findAllWithDetailByStatus(@Param("status") BookStatus status);

    @Query("SELECT c FROM BookCopy c JOIN FETCH c.bookDetail WHERE c.id = :id")
    Optional<BookCopy> findByIdWithDetail(@Param("id") String id);
}
