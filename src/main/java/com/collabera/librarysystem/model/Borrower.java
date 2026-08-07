package com.collabera.librarysystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "borrowers")
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "libraryId is required")
    @Column(name = "library_id", nullable = false, unique = true, length = 100)
    private String libraryId;

    @NotBlank(message = "name is required")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    public Borrower() {
    }

    public Borrower(String libraryId, String name, String email) {
        this.libraryId = libraryId;
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
