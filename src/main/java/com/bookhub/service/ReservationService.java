package com.bookhub.service;

import com.bookhub.dto.reservation.ReservationResponse;
import com.bookhub.entity.*;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.ResourceNotFoundException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.ReservationRepository;
import com.bookhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service gérant les réservations de livres.
 *
 * Règles de gestion :
 *  - Réserver uniquement si le livre est indisponible
 *  - Maximum 5 réservations actives par utilisateur
 *  - Pas de doublon (même livre déjà réservé en attente)
 *  - File d'attente : position calculée automatiquement
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private static final int MAX_RESERVATIONS = 5;

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    // ----------------------------------------------------------------
    // RÉSERVER un livre
    // ----------------------------------------------------------------

    @Transactional
    public ReservationResponse reserveBook(Long bookId) {
        User user = getCurrentUser();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé : " + bookId));

        // On réserve uniquement si le livre est indisponible
        if (book.isAvailable()) {
            throw new BusinessException(
                "Ce livre est disponible, vous pouvez l'emprunter directement");
        }

        // Pas de doublon : déjà une réservation en attente sur ce livre
        boolean alreadyReserved = reservationRepository
                .existsByUserIdAndBookIdAndStatus(user.getId(), bookId, ReservationStatus.WAITING);
        if (alreadyReserved) {
            throw new BusinessException("Vous avez déjà une réservation en attente pour ce livre");
        }

        // Max 5 réservations actives
        long activeReservations = reservationRepository
                .countByUserAndStatus(user, ReservationStatus.WAITING);
        if (activeReservations >= MAX_RESERVATIONS) {
            throw new BusinessException(
                "Vous avez atteint le maximum de " + MAX_RESERVATIONS + " réservations simultanées");
        }

        // Position dans la file = nombre de réservations en attente sur ce livre + 1
        int queuePosition = reservationRepository
                .findByBookIdAndStatusOrderByQueuePosition(bookId, ReservationStatus.WAITING)
                .size() + 1;

        Reservation reservation = Reservation.builder()
                .user(user)
                .book(book)
                .status(ReservationStatus.WAITING)
                .queuePosition(queuePosition)
                .build();

        return toResponse(reservationRepository.save(reservation));
    }

    // ----------------------------------------------------------------
    // MES RÉSERVATIONS
    // ----------------------------------------------------------------

    public List<ReservationResponse> getMyReservations() {
        User user = getCurrentUser();
        return reservationRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // ANNULER une réservation
    // ----------------------------------------------------------------

    @Transactional
    public void cancelReservation(Long reservationId) {
        User user = getCurrentUser();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Réservation non trouvée : " + reservationId));

        // L'utilisateur ne peut annuler que ses propres réservations
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Vous ne pouvez pas annuler cette réservation");
        }

        if (reservation.getStatus() != ReservationStatus.WAITING) {
            throw new BusinessException("Cette réservation ne peut plus être annulée");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // Recalculer les positions dans la file pour ce livre
        reorderQueue(reservation.getBook().getId(), reservation.getQueuePosition());
    }

    // ----------------------------------------------------------------
    // UTILITAIRES PRIVÉS
    // ----------------------------------------------------------------

    /**
     * Après annulation, décrémente la position de tous les suivants dans la file.
     * Ex : file [1, 2, 3, 4] → annulation du 2 → [1, 2, 3]
     */
    private void reorderQueue(Long bookId, int cancelledPosition) {
        List<Reservation> waitingAfter = reservationRepository
                .findByBookIdAndStatusOrderByQueuePosition(bookId, ReservationStatus.WAITING)
                .stream()
                .filter(r -> r.getQueuePosition() > cancelledPosition)
                .toList();

        waitingAfter.forEach(r -> r.setQueuePosition(r.getQueuePosition() - 1));
        reservationRepository.saveAll(waitingAfter);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
    }

    private ReservationResponse toResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .bookId(r.getBook().getId())
                .bookTitle(r.getBook().getTitle())
                .bookAuthor(r.getBook().getAuthor())
                .bookCoverUrl(r.getBook().getCoverUrl())
                .status(r.getStatus())
                .queuePosition(r.getQueuePosition())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
