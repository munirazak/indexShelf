package com.collabera.librarysystem.repository;

import com.collabera.librarysystem.model.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, String> {

    @Query("SELECT c FROM BookCopy c JOIN FETCH c.bookDetail")
    List<BookCopy> findAllWithDetail();
}
