# Library System API

Spring Boot library management API for borrowers and books (register, search, borrow, return).

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL (e.g. Docker: `library-postgres` on port `5432`)

## Run

```bash
# Dev profile (default)
mvn spring-boot:run

# Prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Base URL: `http://localhost:8080`

### Tests and coverage

```bash
# Run unit tests and generate JaCoCo coverage report
mvn test

# Enforce minimum line coverage (80% of measured classes)
mvn verify
```

Coverage report: `target/site/jacoco/index.html`

### Profiles

| Profile | Config |
|---------|--------|
| `dev` (default) | Local Postgres, SQL logging on |
| `prod` | Uses `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |

Database name comes from `spring.datasource.url`. On startup, the app creates the database (if missing) and tables from `schema.sql`.

---

## Borrowers

### Register borrower

`POST /api/borrowers`

```bash
curl -X POST http://localhost:8080/api/borrowers \
  -H "Content-Type: application/json" \
  -d '{
    "libraryId": "LIB-001",
    "name": "Alice Tan",
    "email": "alice@example.com"
  }'
```

**Response:** `201 Created`

```json
{
  "libraryId": "LIB-001",
  "name": "Alice Tan",
  "email": "alice@example.com"
}
```

### List borrowers

`GET /api/borrowers`

```bash
curl http://localhost:8080/api/borrowers
```

---

## Books

### Register book

`POST /api/books`

Creates a `book_detail` (isbn, title, author) if needed, and a `book_copy` (id, status).  
Same ISBN is allowed only when title and author match an existing detail.

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "id": "BOOK-001",
    "isbn": "978-0134685991",
    "title": "Effective Java",
    "author": "Joshua Bloch"
  }'
```

**Response:** `201 Created`

```json
{
  "id": "BOOK-001",
  "isbn": "978-0134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "status": "AVAILABLE"
}
```

### Import books from file

`POST /api/books/from-file`

Accepts `.txt` or `.dat` (multipart field: `file`). Format:

```text
id|isbn|title|author
BOOK-001|978-0134685991|Effective Java|Joshua Bloch
```

```bash
curl -X POST http://localhost:8080/api/books/from-file \
  -F "file=@src/main/resources/sample-books.txt"
```

**Response:** `201 Created` (list of saved books)

### Search / list books

`GET /api/books?status={all|available|borrowed}`

`libraryId` is not returned on this endpoint.

```bash
# All books (default)
curl http://localhost:8080/api/books

# Available only
curl "http://localhost:8080/api/books?status=available"

# Borrowed only
curl "http://localhost:8080/api/books?status=borrowed"
```

### Borrow a book

`POST /api/books/borrow`

Validates borrower and book copy. Book must be `AVAILABLE`. Uses a DB row lock so only one borrower can take the same book id at a time.

```bash
curl -X POST http://localhost:8080/api/books/borrow \
  -H "Content-Type: application/json" \
  -d '{
    "libraryId": "LIB-001",
    "bookId": "BOOK-001"
  }'
```

**Response:** `200 OK`

```json
{
  "id": "BOOK-001",
  "isbn": "978-0134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "status": "BORROWED",
  "libraryId": "LIB-001"
}
```

### Return a book

`POST /api/books/return`

Validates borrower and book. Book must be borrowed by that `libraryId`. Sets status to `AVAILABLE` and clears `libraryId`.

```bash
curl -X POST http://localhost:8080/api/books/return \
  -H "Content-Type: application/json" \
  -d '{
    "libraryId": "LIB-001",
    "bookId": "BOOK-001"
  }'
```

**Response:** `200 OK`

```json
{
  "id": "BOOK-001",
  "isbn": "978-0134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "status": "AVAILABLE"
}
```

---

## Typical flow

1. Register a borrower → `POST /api/borrowers`
2. Register or import books → `POST /api/books` or `POST /api/books/from-file`
3. List available books → `GET /api/books?status=available`
4. Borrow → `POST /api/books/borrow`
5. Return → `POST /api/books/return`

---

## Error responses

| HTTP | When |
|------|------|
| `400` | Validation failed, invalid status, bad book file |
| `404` | Borrower or book copy not found |
| `409` | Duplicate borrower/book, book not available, invalid return, concurrent lock conflict |

Example:

```json
{
  "timestamp": "2026-08-02T10:00:00Z",
  "status": 409,
  "error": "Book copy with id 'BOOK-001' is not available"
}
```

Validation errors also include `fieldErrors`.

---

## Data model (summary)

| Table | Fields |
|-------|--------|
| `borrowers` | `library_id`, `name`, `email` |
| `book_detail` | `isbn`, `title`, `author` |
| `book_copy` | `id`, `isbn`, `status` (`AVAILABLE` \| `BORROWED`), `library_id` (nullable) |
