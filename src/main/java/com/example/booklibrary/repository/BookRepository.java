package com.example.booklibrary.repository;

import com.example.booklibrary.model.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByAvailable(boolean available);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);
}
