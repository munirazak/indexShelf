-- Tables only. Database name comes from spring.datasource.url (via SchemaBootstrap).
-- book_detail / book_copy are owned by BookManagement.

CREATE TABLE IF NOT EXISTS borrowers (
    id         BIGSERIAL PRIMARY KEY,
    library_id VARCHAR(100) NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS borrower_credentials (
    id          BIGSERIAL PRIMARY KEY,
    library_id  VARCHAR(100) NOT NULL UNIQUE REFERENCES borrowers (library_id) ON DELETE CASCADE,
    username    VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL
);
