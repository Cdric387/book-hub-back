package com.bookhub.controller;

import com.bookhub.dto.reservation.ReservationResponse;
import com.bookhub.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * POST /api/reservations/{bookId}   → réserver un livre
 * GET  /api/reservations/my         → mes réservations
 * DELETE /api/reservations/{id}     → annuler une réservation
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Réservations", description = "Gestion des réservations de livres")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{bookId}")
    @Operation(summary = "Réserver un livre indisponible")
    public ResponseEntity<ReservationResponse> reserveBook(@PathVariable Long bookId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.reserveBook(bookId));
    }

    @GetMapping("/my")
    @Operation(summary = "Mes réservations avec rang dans la file")
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {
        return ResponseEntity.ok(reservationService.getMyReservations());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler une réservation")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}
