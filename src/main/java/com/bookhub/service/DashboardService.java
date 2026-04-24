package com.bookhub.service;

import com.bookhub.dto.dashboard.LibrarianDashboardResponse;
import com.bookhub.dto.dashboard.ReaderDashboardResponse;
import com.bookhub.dto.loan.LoanResponse;
import com.bookhub.dto.reservation.ReservationResponse;
import com.bookhub.entity.*;
import com.bookhub.exception.BusinessException;
import com.bookhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service des tableaux de bord.
 * Agrège les statistiques pour chaque type d'utilisateur.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    // ----------------------------------------------------------------
    // DASHBOARD LECTEUR
    // ----------------------------------------------------------------

    public ReaderDashboardResponse getReaderDashboard() {
        User user = getCurrentUser();

        List<Loan> activeLoans = loanRepository.findByUserAndStatus(user, LoanStatus.ACTIVE);
        List<Loan> returnedLoans = loanRepository.findByUserAndStatus(user, LoanStatus.RETURNED);
        List<Reservation> activeReservations = reservationRepository
                .findByUserAndStatus(user, ReservationStatus.WAITING);

        long overdueCount = activeLoans.stream().filter(Loan::isOverdue).count();

        return ReaderDashboardResponse.builder()
                .totalBooksRead(returnedLoans.size())
                .activeLoans(activeLoans.size())
                .overdueLoans((int) overdueCount)
                .activeReservations(activeReservations.size())
                .currentLoans(activeLoans.stream().map(this::toLoanResponse).toList())
                .currentReservations(activeReservations.stream()
                        .map(this::toReservationResponse).toList())
                .build();
    }

    // ----------------------------------------------------------------
    // DASHBOARD BIBLIOTHÉCAIRE / ADMIN
    // ----------------------------------------------------------------

    public LibrarianDashboardResponse getLibrarianDashboard() {
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();

        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
        List<Loan> overdueLoans = activeLoans.stream()
                .filter(Loan::isOverdue)
                .toList();

        // Top 10 livres les plus empruntés
        List<Map<String, Object>> topBooks = loanRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        l -> l.getBook().getTitle(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> Map.<String, Object>of(
                        "title", e.getKey(),
                        "count", e.getValue()))
                .toList();

        return LibrarianDashboardResponse.builder()
                .totalBooks(totalBooks)
                .totalUsers(totalUsers)
                .activeLoans(activeLoans.size())
                .overdueLoans(overdueLoans.size())
                .overdueList(overdueLoans.stream().map(this::toLoanResponse).toList())
                .topBooks(topBooks)
                .build();
    }

    // ----------------------------------------------------------------
    // UTILITAIRES PRIVÉS
    // ----------------------------------------------------------------

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
    }

    private LoanResponse toLoanResponse(Loan loan) {
        long daysOverdue = 0;
        if (loan.isOverdue()) {
            daysOverdue = java.time.temporal.ChronoUnit.DAYS
                    .between(loan.getDueDate(), java.time.LocalDate.now());
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

    private ReservationResponse toReservationResponse(Reservation r) {
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
