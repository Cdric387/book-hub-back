package com.bookhub.service;

import com.bookhub.dto.loan.LoanResponse;
import com.bookhub.entity.*;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.ResourceNotFoundException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.LoanRepository;
import com.bookhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service gérant la logique métier des emprunts.
 *
 * Règles de gestion (cahier des charges) :
 *  RG-LOAN-01 : Maximum 3 emprunts actifs par utilisateur
 *  RG-LOAN-02 : Durée d'emprunt = 14 jours
 *  RG-LOAN-03 : Bloqué si l'utilisateur a un emprunt en retard
 *  RG-LOAN-04 : Un même livre peut être ré-emprunté après retour
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    private static final int MAX_ACTIVE_LOANS = 3;
    private static final int LOAN_DURATION_DAYS = 14;

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    // ----------------------------------------------------------------
    // EMPRUNTER un livre
    // ----------------------------------------------------------------

    /**
     * Crée un emprunt pour l'utilisateur connecté.
     * Vérifie toutes les règles de gestion avant de procéder.
     */
    @Transactional
    public LoanResponse borrowBook(Long bookId) {
        User user = getCurrentUser();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé : " + bookId));

        // RG : livre disponible
        if (!book.isAvailable()) {
            throw new BusinessException("Ce livre n'est pas disponible à l'emprunt");
        }

        // RG-LOAN-01 : max 3 emprunts actifs
        long activeLoans = loanRepository.countByUserAndStatus(user, LoanStatus.ACTIVE);
        if (activeLoans >= MAX_ACTIVE_LOANS) {
            throw new BusinessException(
                "Vous avez atteint le maximum de " + MAX_ACTIVE_LOANS + " emprunts simultanés");
        }

        // RG-LOAN-03 : bloqué si retard en cours
        boolean hasOverdue = loanRepository.findByUserAndStatus(user, LoanStatus.ACTIVE)
                .stream().anyMatch(Loan::isOverdue);
        if (hasOverdue) {
            throw new BusinessException(
                "Vous avez un emprunt en retard. Veuillez retourner le livre avant d'en emprunter un nouveau");
        }

        // Tout est OK : créer l'emprunt
        LocalDate today = LocalDate.now();
        Loan loan = Loan.builder()
                .user(user)
                .book(book)
                .borrowedAt(today)
                .dueDate(today.plusDays(LOAN_DURATION_DAYS))  // RG-LOAN-02
                .status(LoanStatus.ACTIVE)
                .build();

        // Décrémenter le stock
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return toResponse(loanRepository.save(loan));
    }

    // ----------------------------------------------------------------
    // MES EMPRUNTS (utilisateur connecté)
    // ----------------------------------------------------------------

    /** Emprunts actifs de l'utilisateur connecté */
    public List<LoanResponse> getMyActiveLoans() {
        User user = getCurrentUser();
        return loanRepository.findByUserAndStatus(user, LoanStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Historique complet des emprunts de l'utilisateur connecté */
    public List<LoanResponse> getMyLoanHistory() {
        User user = getCurrentUser();
        return loanRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // RETOUR (bibliothécaire)
    // ----------------------------------------------------------------

    /**
     * Enregistre le retour d'un livre.
     * Calcule automatiquement le retard, libère un exemplaire.
     */
    @Transactional
    public LoanResponse returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt non trouvé : " + loanId));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException("Cet emprunt est déjà clôturé");
        }

        LocalDate today = LocalDate.now();
        loan.setReturnedAt(today);
        loan.setStatus(LoanStatus.RETURNED);

        // Incrémenter le stock
        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return toResponse(loanRepository.save(loan));
    }

    // ----------------------------------------------------------------
    // TOUS LES EMPRUNTS (bibliothécaire)
    // ----------------------------------------------------------------

    /** Liste tous les emprunts actifs — accès bibliothécaire */
    public List<LoanResponse> getAllActiveLoans() {
        return loanRepository.findByStatus(LoanStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Liste tous les emprunts en retard — accès bibliothécaire */
    public List<LoanResponse> getOverdueLoans() {
        return loanRepository.findByStatus(LoanStatus.ACTIVE)
                .stream()
                .filter(Loan::isOverdue)
                .map(this::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // UTILITAIRES PRIVÉS
    // ----------------------------------------------------------------

    /** Récupère l'utilisateur connecté depuis le contexte de sécurité */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
    }

    /** Convertit une entité Loan en DTO de réponse */
    private LoanResponse toResponse(Loan loan) {
        long daysOverdue = 0;
        if (loan.isOverdue()) {
            daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
        }

        return LoanResponse.builder()
                .id(loan.getId())
                .bookId(loan.getBook().getId())
                .bookTitle(loan.getBook().getTitle())
                .bookAuthor(loan.getBook().getAuthor())
                .bookCoverUrl(loan.getBook().getCoverUrl())
                .borrowedAt(loan.getBorrowedAt())
                .dueDate(loan.getDueDate())
                .returnedAt(loan.getReturnedAt())
                .status(loan.getStatus())
                .overdue(loan.isOverdue())
                .daysOverdue(daysOverdue)
                .build();
    }
}
