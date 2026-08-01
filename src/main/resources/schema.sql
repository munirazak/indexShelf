CREATE DATABASE kopibru_book_catalogue;

CREATE TABLE IF NOT EXISTS borrowers (
    library_id VARCHAR(100) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS books (
    id     VARCHAR(100) PRIMARY KEY,
    isbn   VARCHAR(20)  NOT NULL,
    title  VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL
);

-- ALTER TABLE books DROP CONSTRAINT IF EXISTS uk_books_isbn;
-- ALTER TABLE books DROP CONSTRAINT IF EXISTS books_isbn_key;
