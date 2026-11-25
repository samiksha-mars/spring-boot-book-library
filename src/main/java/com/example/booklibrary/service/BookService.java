package com.example.booklibrary.service;

import com.example.booklibrary.dto.BookRequest;
import com.example.booklibrary.dto.BookResponse;
import com.example.booklibrary.exception.DuplicateIsbnException;
import com.example.booklibrary.model.Book;
import com.example.booklibrary.repository.BookRepository;
import java.util.List;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> search(String title, String author, Boolean available) {
        // Null probe fields are ignored, so only the filters that were supplied apply.
        Book probe = new Book();
        probe.setTitle(title);
        probe.setAuthor(author);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        if (available == null) {
            matcher = matcher.withIgnorePaths("available");
        } else {
            probe.setAvailable(available);
        }
        return repository.findAll(Example.of(probe, matcher)).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return toResponse(repository.findById(id).orElseThrow());
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        String isbn = normalizeIsbn(request.isbn());
        if (isbn != null && repository.existsByIsbn(isbn)) {
            throw new DuplicateIsbnException(isbn);
        }
        Book book = new Book();
        apply(book, request);
        repository.save(book);
        return toResponse(book);
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = repository.findById(id).orElseThrow();
        String isbn = normalizeIsbn(request.isbn());
        if (isbn != null && repository.existsByIsbnAndIdNot(isbn, id)) {
            throw new DuplicateIsbnException(isbn);
        }
        apply(book, request);
        repository.save(book);
        return toResponse(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = repository.findById(id).orElseThrow();
        repository.delete(book);
    }

    private void apply(Book book, BookRequest request) {
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(normalizeIsbn(request.isbn()));
        book.setPublishedYear(request.publishedYear());
        book.setAvailable(Boolean.TRUE.equals(request.available()));
    }

    private static String normalizeIsbn(String isbn) {
        return isbn == null || isbn.isBlank() ? null : isbn.strip();
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublishedYear(),
                book.isAvailable()
        );
    }
}
