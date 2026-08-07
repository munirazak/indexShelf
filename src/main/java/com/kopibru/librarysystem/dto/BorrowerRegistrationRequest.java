package com.kopibru.librarysystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BorrowerRegistrationRequest {

    @NotBlank(message = "libraryId is required")
    private String libraryId;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 100, message = "username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
    private String password;

    public BorrowerRegistrationRequest() {
    }

    public BorrowerRegistrationRequest(String libraryId, String name, String email,
                                       String username, String password) {
        this.libraryId = libraryId;
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
