package com.example.booklibrary.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.booklibrary.model.Book;
import com.example.booklibrary.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository repository;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    private Book save(String title, String author, String isbn, boolean available) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setAvailable(available);
        return repository.save(book);
    }

    @Test
    void createReturns201WithTheSavedBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Clean Code", "author": "Robert C. Martin",
                                 "isbn": "9780132350884", "publishedYear": 2008, "available": true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.publishedYear").value(2008))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void availableDefaultsToFalseWhenOmitted() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Clean Code", "author": "Robert C. Martin"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void getReturnsBookById() throws Exception {
        Book book = save("Refactoring", "Martin Fowler", "9780134757599", true);

        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Refactoring"))
                .andExpect(jsonPath("$.author").value("Martin Fowler"));
    }

    @Test
    void updateReplacesFields() throws Exception {
        Book book = save("Refactoring", "Martin Fowler", "9780134757599", true);

        mockMvc.perform(put("/api/books/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Refactoring (2nd ed.)", "author": "Martin Fowler",
                                 "isbn": "9780134757599", "publishedYear": 2018, "available": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Refactoring (2nd ed.)"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void deleteReturns204AndRemovesBook() throws Exception {
        Book book = save("Refactoring", "Martin Fowler", null, true);

        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownIdReturns404ForGetUpdateAndDelete() throws Exception {
        mockMvc.perform(get("/api/books/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
        mockMvc.perform(put("/api/books/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "T", "author": "A", "available": true}
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/books/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidBookIsRejected() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": " ", "author": "A", "publishedYear": 3000, "available": true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.errors.title").value("title must not be blank"))
                .andExpect(jsonPath("$.errors.publishedYear").value("publishedYear must be at most 2100"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void malformedJsonReturnsProblemDetail() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void duplicateIsbnReturns409() throws Exception {
        Book existing = save("Clean Code", "Robert C. Martin", "9780132350884", true);
        Book other = save("Refactoring", "Martin Fowler", "9780134757599", true);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Copy", "author": "Someone", "isbn": "9780132350884"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("A book with ISBN 9780132350884 already exists"));
        mockMvc.perform(put("/api/books/{id}", other.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Refactoring", "author": "Martin Fowler", "isbn": "9780132350884"}
                                """))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/books/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Clean Code", "author": "Robert C. Martin", "isbn": "9780132350884"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void blankIsbnIsStoredAsAbsentSoItNeverConflicts() throws Exception {
        for (String title : new String[] {"First", "Second"}) {
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title": "%s", "author": "A", "isbn": " "}
                                    """.formatted(title)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.isbn").doesNotExist());
        }
    }

    @Test
    void searchesByTitleAuthorAndAvailability() throws Exception {
        save("Clean Code", "Robert C. Martin", null, true);
        save("Clean Architecture", "Robert C. Martin", null, false);
        save("Refactoring", "Martin Fowler", null, true);

        mockMvc.perform(get("/api/books"))
                .andExpect(jsonPath("$", hasSize(3)));
        mockMvc.perform(get("/api/books").param("title", "clean"))
                .andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/api/books").param("author", "FOWLER"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Refactoring"));
        mockMvc.perform(get("/api/books").param("available", "false"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Clean Architecture"));
    }
}
