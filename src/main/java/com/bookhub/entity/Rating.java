package com.bookhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDateTime;

/** Note et commentaire d'un utilisateur sur un livre. */
@Entity
@Table(name = "ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Min(1) @Max(5)
    @Column(nullable = false)
    private int stars;

    @Column(columnDefinition = "NVARCHAR(1000)")
    private String comment;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
