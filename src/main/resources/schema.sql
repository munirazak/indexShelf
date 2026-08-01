CREATE DATABASE kopibru_book_catalogue;

CREATE TABLE IF NOT EXISTS borrowers (
    library_id VARCHAR(100) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL
);
