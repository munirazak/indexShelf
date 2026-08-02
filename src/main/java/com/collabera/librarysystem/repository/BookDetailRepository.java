package com.collabera.librarysystem.repository;

import com.collabera.librarysystem.model.BookDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDetailRepository extends JpaRepository<BookDetail, String> {
}
