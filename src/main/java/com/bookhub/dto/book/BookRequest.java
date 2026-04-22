package com.bookhub.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BookRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotBlank(message = "L'auteur est obligatoire")
    private String author;

    private String isbn;
    private String description;
    private String category;
    private String coverUrl;
    private LocalDate publishedDate;

    @Positive(message = "Le nombre d'exemplaires doit être positif")
    private int totalCopies = 1;
}
