package com.bookhub.dto.book;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String category;
    private String coverUrl;
    private LocalDate publishedDate;
    private int totalCopies;
    private int availableCopies;
    private boolean available;
    private Double averageRating;
    private LocalDateTime createdAt;
}
