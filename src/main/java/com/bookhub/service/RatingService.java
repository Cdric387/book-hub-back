package com.bookhub.service;

import com.bookhub.dto.rating.RatingRequest;
import com.bookhub.dto.rating.RatingResponse;
import com.bookhub.entity.*;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.ResourceNotFoundException;
import com.bookhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service de notation et commentaires.
 *
 * Règles métier :
 *  - Seul un lecteur ayant emprunté le livre peut le noter
 *  - Une seule note par utilisateur par livre (modifiable)
 *  - La note moyenne est calculée dynamiquement
 *  - Un bibliothécaire peut supprimer tout commentaire
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    // ----------------------------------------------------------------
    // NOTER / COMMENTER un livre
    // ----------------------------------------------------------------

    @Transactional
    public RatingResponse rateBook(Long bookId, RatingRequest request) {
        User user = getCurrentUser();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé : " + bookId));

        // Vérifier que l'utilisateur a déjà emprunté ce livre
        boolean hasEverBorrowed = loanRepository
                .existsByUserAndBookIdAndStatus(user, bookId, LoanStatus.RETURNED);
        if (!hasEverBorrowed) {
            throw new BusinessException(
                "Vous devez avoir emprunté et retourné ce livre pour pouvoir le noter");
        }

        // Si une note existe déjà → mise à jour
        Rating rating = ratingRepository
                .findByUserIdAndBookId(user.getId(), bookId)
                .orElse(Rating.builder().user(user).book(book).build());

        rating.setStars(request.getStars());
        rating.setComment(request.getComment());
        if (rating.getId() != null) {
            rating.setUpdatedAt(LocalDateTime.now());
        }

        return toResponse(ratingRepository.save(rating));
    }

    // ----------------------------------------------------------------
    // LIRE les avis d'un livre
    // ----------------------------------------------------------------

    public List<RatingResponse> getBookRatings(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Livre non trouvé : " + bookId);
        }
        return ratingRepository.findByBookId(bookId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Double getBookAverageRating(Long bookId) {
        return ratingRepository.findAverageStarsByBookId(bookId);
    }

    // ----------------------------------------------------------------
    // SUPPRIMER un avis (bibliothécaire = modération, user = le sien)
    // ----------------------------------------------------------------

    @Transactional
    public void deleteRating(Long ratingId) {
        User user = getCurrentUser();
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis non trouvé : " + ratingId));

        boolean isOwner = rating.getUser().getId().equals(user.getId());
        boolean isLibrarianOrAdmin = user.getRole() == Role.LIBRARIAN
                || user.getRole() == Role.ADMIN;

        if (!isOwner && !isLibrarianOrAdmin) {
            throw new BusinessException("Vous ne pouvez pas supprimer cet avis");
        }

        ratingRepository.delete(rating);
    }

    // ----------------------------------------------------------------
    // UTILITAIRES PRIVÉS
    // ----------------------------------------------------------------

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
    }

    private RatingResponse toResponse(Rating r) {
        return RatingResponse.builder()
                .id(r.getId())
                .bookId(r.getBook().getId())
                .bookTitle(r.getBook().getTitle())
                .userId(r.getUser().getId())
                .userFirstName(r.getUser().getFirstName())
                .userLastName(r.getUser().getLastName())
                .stars(r.getStars())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
