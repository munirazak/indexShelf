package com.collabera.librarysystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @NotBlank(message = "id is required")
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @NotBlank(message = "isbn is required")
    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;

    @NotBlank(message = "title is required")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotBlank(message = "author is required")
    @Column(name = "author", nullable = false, length = 255)
    private String author;

    public Book() {
    }

    public Book(String id, String isbn, String title, String author) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
