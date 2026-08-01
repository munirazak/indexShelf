package com.collabera.librarysystem.repository;

import com.collabera.librarysystem.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    boolean existsById(String id);

    List<Book> findByIsbn(String isbn);
}
