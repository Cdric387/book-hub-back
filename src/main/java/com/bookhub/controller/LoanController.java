package com.bookhub.controller;

import com.bookhub.dto.loan.LoanResponse;
import com.bookhub.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des emprunts.
 *
 * POST /api/loans/{bookId}          → emprunter (USER)
 * GET  /api/loans/my                → mes emprunts actifs (USER)
 * GET  /api/loans/my/history        → mon historique (USER)
 * GET  /api/loans                   → tous les emprunts actifs (LIBRARIAN)
 * GET  /api/loans/overdue           → emprunts en retard (LIBRARIAN)
 * PUT  /api/loans/{id}/return       → enregistrer un retour (LIBRARIAN)
 */
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Emprunts", description = "Gestion des emprunts de livres")
@SecurityRequirement(name = "bearerAuth")
public class LoanController {

    private final LoanService loanService;

    // ---- USER ----

    @PostMapping("/{bookId}")
    @Operation(summary = "Emprunter un livre")
    public ResponseEntity<LoanResponse> borrowBook(@PathVariable Long bookId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.borrowBook(bookId));
    }

    @GetMapping("/my")
    @Operation(summary = "Mes emprunts en cours")
    public ResponseEntity<List<LoanResponse>> getMyActiveLoans() {
        return ResponseEntity.ok(loanService.getMyActiveLoans());
    }

    @GetMapping("/my/history")
    @Operation(summary = "Mon historique d'emprunts")
    public ResponseEntity<List<LoanResponse>> getMyLoanHistory() {
        return ResponseEntity.ok(loanService.getMyLoanHistory());
    }

    // ---- LIBRARIAN / ADMIN ----

    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Tous les emprunts actifs (bibliothécaire)")
    public ResponseEntity<List<LoanResponse>> getAllActiveLoans() {
        return ResponseEntity.ok(loanService.getAllActiveLoans());
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Emprunts en retard (bibliothécaire)")
    public ResponseEntity<List<LoanResponse>> getOverdueLoans() {
        return ResponseEntity.ok(loanService.getOverdueLoans());
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Enregistrer le retour d'un livre")
    public ResponseEntity<LoanResponse> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnBook(id));
    }
}
