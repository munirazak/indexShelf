package com.collabera.librarysystem.dto;

public class LoginResponse {

    private String token;
    private String libraryId;
    private String username;

    public LoginResponse() {
    }

    public LoginResponse(String token, String libraryId, String username) {
        this.token = token;
        this.libraryId = libraryId;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
