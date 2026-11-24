package com.example.booklibrary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotBlank(message = "title must not be blank")
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,
        @NotBlank(message = "author must not be blank")
        @Size(max = 200, message = "author must be at most 200 characters")
        String author,
        @Size(max = 20, message = "isbn must be at most 20 characters")
        String isbn,
        @Min(value = 1000, message = "publishedYear must be at least 1000")
        @Max(value = 2100, message = "publishedYear must be at most 2100")
        Integer publishedYear,
        boolean available
) {
}
