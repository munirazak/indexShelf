# Library System API

Spring Boot library management API for borrowers and books (register, login, search, borrow, return), with JWT-based authentication.

**Maven coordinates:** `com.kopibru:library-system`  
**Base package:** `com.kopibru.librarysystem`

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL **or** Docker / Docker Compose

## Why PostgreSQL?

This library system is transactional: borrowers, book copies, and borrow/return must stay consistent. A relational database fits that model well.

PostgreSQL was chosen because:

- **Relational domain** — clear relationships between borrowers, credentials, book details, and copies
- **Strong consistency** — borrow/return need atomic updates so only one member can hold a given copy at a time
- **Concurrent safety** — row-level locking (`SELECT … FOR UPDATE`) is used when borrowing; PostgreSQL handles this reliably
- **Spring Boot + Docker fit** — well supported by JPA/Hibernate and easy to run via Docker Compose for local and CI/CD demos

MySQL would also work for this scale. The important choice is a relational store with transactions; PostgreSQL is a solid, common default for that in modern Spring Boot projects.

## Assumptions

Assumptions for requirements **not explicitly stated** in the task brief are listed below, grouped by topic.

### Data models

| Topic | Assumption |
|-------|------------|
| Borrower primary key | Surrogate `id` (`BIGSERIAL`). Business key `libraryId` remains unique and is supplied by the client on registration. |
| Borrower credentials | Stored in `borrower_credentials` (`username`, BCrypt-hashed `password`), linked to `borrowers.library_id` with `ON DELETE CASCADE`. |
| Borrower email | Must be present and syntactically valid (`@Email`). Duplicate emails are allowed unless they share the same `libraryId`. |
| Book unique id | Interpreting “Book” for borrow/return as a **physical copy**. Unique id is `book_copy.id` (string, client-supplied). |
| ISBN + title + author | Stored once in `book_detail` (ISBN is the primary key of that table). |
| Multiple copies of same ISBN | Implemented as multiple `book_copy` rows sharing the same ISBN, each with its own unique `id`. |
| Same ISBN rule | If an ISBN already exists, a new copy is allowed only when title **and** author match the existing `book_detail`; otherwise registration is rejected (`409`). |
| Different ISBN, same title/author | Allowed — treated as different books (separate `book_detail` rows), per the brief. |

### Auth (JWT)

| Topic | Assumption |
|-------|------------|
| Login | `POST /api/auth/login` with username/password returns a JWT whose subject is `libraryId`. |
| Password storage | Passwords are hashed with BCrypt; never returned in API responses. |
| Borrow / return identity | Taken from the JWT (`Authorization: Bearer …`), not from the request body — prevents acting as another borrower. |
| Public endpoints | `POST /api/auth/login`, `POST /api/borrowers`, `GET /api/books` are unauthenticated. Other endpoints require a valid JWT. |
| Multiple tokens | Stateless JWTs: logging in again issues a new token; existing unexpired tokens remain valid until `app.jwt.expiration-ms`. |

### Register borrower / register book

| Topic | Assumption |
|-------|------------|
| Duplicate borrower | Registering an existing `libraryId` or `username` is rejected (`409`). |
| Registration payload | Client sends `libraryId`, `name`, `email`, `username`, and `password` (min 8 chars). |
| Duplicate book copy id | Registering an existing `book_copy.id` is rejected (`409`). |
| Book registration payload | Client sends `id`, `isbn`, `title`, and `author` in one request; the system creates/reuses `book_detail` and creates a new `book_copy`. |
| Default copy status | New copies are `AVAILABLE` unless a status is explicitly provided. |
| Extra: file import | An optional `POST /api/books/from-file` imports books from `.txt`/`.dat` (`id\|isbn\|title\|author`). Not required by the brief; added for convenience. Requires JWT. |

### List books

| Topic | Assumption |
|-------|------------|
| “All books” | Means all **copies** (`book_copy`), joined with title/author/ISBN from `book_detail`. |
| Status filter | Optional query `?status=all\|available\|borrowed` (default `all`). Not required by the brief; added so borrowers can find available copies. |
| Hide borrower on list | `GET /api/books` does **not** return `libraryId`, even for borrowed copies (privacy / list clarity). |

### Borrow / return (authenticated borrower)

| Topic | Assumption |
|-------|------------|
| Required inputs | Body requires only `bookId`. Borrower is the authenticated `libraryId` from the JWT. |
| Borrower must exist | Unknown `libraryId` (token subject) → `404`. |
| Book copy must exist | Unknown `bookId` → `404`. |
| Borrow only if available | Borrow is allowed only when status is `AVAILABLE`; otherwise `409`. |
| One borrower per copy | A copy stores at most one `library_id` while borrowed; status becomes `BORROWED`. |
| Concurrent borrows | Pessimistic DB locking ensures only one successful borrow of the same `bookId` at a time. |
| Return ownership | Return is allowed only if the copy is `BORROWED` **by that same** authenticated `libraryId`; otherwise `409`. |
| After return | Status set to `AVAILABLE` and `library_id` cleared. |
| Status naming | Used `AVAILABLE` / `BORROWED` (not specified in the brief). |

### API & platform (beyond the brief)

| Topic | Assumption |
|-------|------------|
| Style | RESTful JSON over HTTP; Spring Boot 3 + JPA + Spring Security. |
| Persistence | PostgreSQL; schema created/updated on startup from `schema.sql` + profile settings. |
| Validation / errors | Bean validation on inputs; consistent JSON error body with appropriate HTTP status codes. |
| Environments | `dev` / `prod` Spring profiles; prod DB credentials via environment variables. JWT secret via `JWT_SECRET` / `app.jwt.secret`. |
| Containerization / CI/CD | Docker, Docker Compose, and GitHub Actions are implementation choices to demonstrate declarative containerization and CI/CD — not stated in the library API brief. |

## Run

### Option A — Local (Maven)

```bash
# Dev profile (default) — requires local PostgreSQL
mvn spring-boot:run

# Prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Option B — Docker Compose (app + Postgres)

```bash
docker compose up --build
```

This starts:

- PostgreSQL on port `5432`
- Library API on port `8080` (`prod` profile)

Stop with `docker compose down` (add `-v` to also remove the DB volume).

Base URL: `http://localhost:8080`

### Tests and coverage

```bash
# Run unit tests and generate JaCoCo coverage report
mvn test

# Enforce minimum line coverage (80% of measured classes)
mvn verify
```

Coverage report: `target/site/jacoco/index.html`

### CI/CD (GitHub Actions)

Declarative pipeline in `.github/workflows/ci.yml`:

1. On push/PR to `main`: run `mvn verify` (tests + coverage gate)
2. On push to `main`: build and publish the Docker image to GitHub Container Registry (`ghcr.io`)

Pull the published image (after a successful push to `main`):

```bash
docker pull ghcr.io/<your-github-username>/library-system:latest
```

### Profiles

| Profile | Config |
|---------|--------|
| `dev` (default) | Local Postgres, SQL logging on |
| `prod` | Uses `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |

| JWT setting | Config |
|-------------|--------|
| `app.jwt.secret` / `JWT_SECRET` | Signing key (min 32 characters) |
| `app.jwt.expiration-ms` / `JWT_EXPIRATION_MS` | Token lifetime (default 1 hour) |

Database name comes from `spring.datasource.url`. On startup, the app creates the database (if missing) and tables from `schema.sql`.

---

## Auth

### Login

`POST /api/auth/login` (public)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "password123"
  }'
```

**Response:** `200 OK`

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "libraryId": "LIB-001",
  "username": "alice"
}
```

Use the token on protected endpoints:

```bash
-H "Authorization: Bearer <token>"
```

Invalid credentials → `401 Unauthorized`.

---

## Borrowers

### Register borrower

`POST /api/borrowers` (public)

Creates the borrower and hashed credentials in one transaction.

```bash
curl -X POST http://localhost:8080/api/borrowers \
  -H "Content-Type: application/json" \
  -d '{
    "libraryId": "LIB-001",
    "name": "Alice Tan",
    "email": "alice@example.com",
    "username": "alice",
    "password": "password123"
  }'
```

**Response:** `201 Created` (password is never returned)

```json
{
  "id": 1,
  "libraryId": "LIB-001",
  "name": "Alice Tan",
  "email": "alice@example.com"
}
```

### List borrowers

`GET /api/borrowers` (requires JWT)

```bash
curl http://localhost:8080/api/borrowers \
  -H "Authorization: Bearer <token>"
```

---

## Books

### Register book

`POST /api/books` (requires JWT)

Creates a `book_detail` (isbn, title, author) if needed, and a `book_copy` (id, status).  
Same ISBN is allowed only when title and author match an existing detail.

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer <token>" \
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

`POST /api/books/from-file` (requires JWT)

Accepts `.txt` or `.dat` (multipart field: `file`). Format:

```text
id|isbn|title|author
BOOK-001|978-0134685991|Effective Java|Joshua Bloch
```

```bash
curl -X POST http://localhost:8080/api/books/from-file \
  -H "Authorization: Bearer <token>" \
  -F "file=@src/main/resources/sample-books.txt"
```

**Response:** `201 Created` (list of saved books)

### Search / list books

`GET /api/books?status={all|available|borrowed}` (public)

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

`POST /api/books/borrow` (requires JWT)

Uses the authenticated borrower’s `libraryId` from the token. Book must be `AVAILABLE`. Uses a DB row lock so only one borrower can take the same book id at a time.

```bash
curl -X POST http://localhost:8080/api/books/borrow \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
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

`POST /api/books/return` (requires JWT)

Book must be borrowed by the authenticated `libraryId`. Sets status to `AVAILABLE` and clears `libraryId`.

```bash
curl -X POST http://localhost:8080/api/books/return \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
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
2. Login → `POST /api/auth/login` (save the `token`)
3. Register or import books → `POST /api/books` or `POST /api/books/from-file` (with Bearer token)
4. List available books → `GET /api/books?status=available`
5. Borrow → `POST /api/books/borrow` (with Bearer token)
6. Return → `POST /api/books/return` (with Bearer token)

---

## Error responses

| HTTP | When |
|------|------|
| `400` | Validation failed, invalid status, bad book file |
| `401` | Missing/invalid JWT, or bad login credentials |
| `404` | Borrower or book copy not found |
| `409` | Duplicate borrower/username/book, book not available, invalid return, concurrent lock conflict |

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
| `borrowers` | `id` (PK), `library_id` (unique), `name`, `email` |
| `borrower_credentials` | `id` (PK), `library_id` (FK → `borrowers`, cascade delete), `username` (unique), `password` (BCrypt hash) |
| `book_detail` | `isbn`, `title`, `author` |
| `book_copy` | `id`, `isbn`, `status` (`AVAILABLE` \| `BORROWED`), `library_id` (nullable) |

---

## Containerization & CI/CD (declarative)

| File | Purpose |
|------|---------|
| `Dockerfile` | Multi-stage build of the Spring Boot JAR into a runtime image |
| `docker-compose.yml` | Declares app + Postgres services |
| `.github/workflows/ci.yml` | CI (test/coverage) and CD (publish image to GHCR) |
