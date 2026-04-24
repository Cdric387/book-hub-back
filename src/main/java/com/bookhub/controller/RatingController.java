package com.bookhub.controller;

import com.bookhub.dto.rating.RatingRequest;
import com.bookhub.dto.rating.RatingResponse;
import com.bookhub.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * POST   /api/books/{id}/ratings   → noter un livre
 * GET    /api/books/{id}/ratings   → avis d'un livre (public)
 * DELETE /api/ratings/{id}         → supprimer un avis
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Notations", description = "Notes et commentaires sur les livres")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/api/books/{bookId}/ratings")
    @Operation(summary = "Noter et commenter un livre (après l'avoir emprunté)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RatingResponse> rateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody RatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ratingService.rateBook(bookId, request));
    }

    @GetMapping("/api/books/{bookId}/ratings")
    @Operation(summary = "Consulter les avis d'un livre")
    public ResponseEntity<List<RatingResponse>> getBookRatings(@PathVariable Long bookId) {
        return ResponseEntity.ok(ratingService.getBookRatings(bookId));
    }

    @DeleteMapping("/api/ratings/{id}")
    @Operation(summary = "Supprimer un avis (le sien ou modération bibliothécaire)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }
}
