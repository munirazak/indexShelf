package com.collabera.librarysystem.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class Borrower {

    @NotBlank(message = "libraryId is required")
    private String libraryId;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    public Borrower() {
    }

    public Borrower(String libraryId, String name, String email) {
        this.libraryId = libraryId;
        this.name = name;
        this.email = email;
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
