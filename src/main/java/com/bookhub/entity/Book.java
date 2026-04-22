package com.bookhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Livre du catalogue BookHub. */
@Entity
@Table(name = "books")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String author;

    @Column(unique = true)
    private String isbn;

    @Column(columnDefinition = "NVARCHAR(2000)")
    private String description;

    private String category;
    private String coverUrl;
    private LocalDate publishedDate;

    @Positive
    @Column(nullable = false)
    @Builder.Default
    private int totalCopies = 1;

    @Column(nullable = false)
    @Builder.Default
    private int availableCopies = 1;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isAvailable() { return availableCopies > 0; }
}
