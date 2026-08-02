-- Tables only. Database name comes from spring.datasource.url (via SchemaBootstrap).

CREATE TABLE IF NOT EXISTS borrowers (
    library_id VARCHAR(100) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS book_detail (
    isbn   VARCHAR(20)  PRIMARY KEY,
    title  VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS book_copy (
    id         VARCHAR(100) PRIMARY KEY,
    isbn       VARCHAR(20)  NOT NULL REFERENCES book_detail (isbn),
    status     VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    library_id VARCHAR(100) REFERENCES borrowers (library_id)
);