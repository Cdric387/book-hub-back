package com.bookhub.service;

import com.bookhub.dto.book.BookRequest;
import com.bookhub.dto.book.BookResponse;
import com.bookhub.entity.Book;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.ResourceNotFoundException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.LoanRepository;
import com.bookhub.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de gestion du catalogue de livres.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final RatingRepository ratingRepository;

    /** Liste paginée de tous les livres */
    public Page<BookResponse> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::toResponse);
    }

    /** Détail d'un livre par son ID */
    public BookResponse findById(Long id) {
        return bookRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé : " + id));
    }

    /** Recherche textuelle */
    public Page<BookResponse> search(String query, Pageable pageable) {
        return bookRepository.search(query, pageable).map(this::toResponse);
    }

    /** Création d'un livre (bibliothécaire) */
    @Transactional
    public BookResponse create(BookRequest request) {
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .description(request.getDescription())
                .category(request.getCategory())
                .coverUrl(request.getCoverUrl())
                .publishedDate(request.getPublishedDate())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .build();
        return toResponse(bookRepository.save(book));
    }

    /** Mise à jour d'un livre (bibliothécaire) */
    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé : " + id));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setCategory(request.getCategory());
        book.setCoverUrl(request.getCoverUrl());
        book.setPublishedDate(request.getPublishedDate());

        return toResponse(bookRepository.save(book));
    }

    /** Suppression d'un livre — interdit s'il est en cours d'emprunt */
    @Transactional
    public void delete(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé : " + id));

        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new BusinessException("Impossible de supprimer un livre actuellement emprunté");
        }

        bookRepository.delete(book);
    }

    /** Convertit une entité Book en DTO de réponse */
    private BookResponse toResponse(Book book) {
        Double avgRating = ratingRepository.findAverageStarsByBookId(book.getId());
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .category(book.getCategory())
                .coverUrl(book.getCoverUrl())
                .publishedDate(book.getPublishedDate())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .available(book.isAvailable())
                .averageRating(avgRating)
                .createdAt(book.getCreatedAt())
                .build();
    }
}
