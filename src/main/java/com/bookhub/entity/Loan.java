package com.bookhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/** Emprunt d'un livre par un utilisateur. */
@Entity
@Table(name = "loans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate borrowedAt;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoanStatus status = LoanStatus.ACTIVE;

    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE && LocalDate.now().isAfter(dueDate);
    }
}
