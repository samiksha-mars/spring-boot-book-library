package com.example.booklibrary.dto;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Integer publishedYear,
        boolean available
) {
}
