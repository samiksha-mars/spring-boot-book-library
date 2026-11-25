# Spring Boot Book Library

A small REST API for managing a library of books, built with Spring Boot. Books are
persisted to an in-memory H2 database and can be searched by title or author.

## Features

- Create, read, update and delete books
- Search by title and/or author (case-insensitive substring match)
- Filter by availability (combinable with the title/author search)
- Bean Validation on input
- In-memory H2 database with console

## Tech Stack

- Java 17
- Spring Boot (Web MVC, Data JPA, Validation)
- H2 (in-memory database)

## Running

```bash
./mvnw spring-boot:run
```

App starts on http://localhost:8080. H2 console: http://localhost:8080/h2-console
(JDBC URL `jdbc:h2:mem:testdb`, user `sa`, empty password).

## API

| Method | Path            | Description                                   |
|--------|-----------------|-----------------------------------------------|
| GET    | `/api/books`    | List/search (`?title=`, `?author=`, `?available=true`) |
| GET    | `/api/books/{id}` | Get a single book                           |
| POST   | `/api/books`    | Create a book                                 |
| PUT    | `/api/books/{id}` | Update a book                               |
| DELETE | `/api/books/{id}` | Delete a book                               |

### Example

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title": "Clean Code", "author": "Robert C. Martin", "isbn": "9780132350884", "publishedYear": 2008, "available": true}'
```
