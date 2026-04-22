package com.bookhub.repository;

import com.bookhub.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    /** Recherche insensible à la casse sur titre, auteur ou ISBN */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Book> search(@Param("q") String query, Pageable pageable);

    /** Filtre par catégorie */
    Page<Book> findByCategory(String category, Pageable pageable);

    /** Filtre par disponibilité */
    Page<Book> findByAvailableCopiesGreaterThan(int copies, Pageable pageable);
}
