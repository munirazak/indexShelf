package com.kopibru.librarysystem.repository;

import com.kopibru.librarysystem.model.BookDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDetailRepository extends JpaRepository<BookDetail, String> {
}
