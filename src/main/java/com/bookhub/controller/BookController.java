package com.bookhub.controller;

import com.bookhub.dto.book.BookRequest;
import com.bookhub.dto.book.BookResponse;
import com.bookhub.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion du catalogue de livres.
 * GET publics — POST/PUT réservés LIBRARIAN — DELETE réservé ADMIN.
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Catalogue", description = "Gestion des livres")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Liste paginée des livres (20/page)")
    public ResponseEntity<Page<BookResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sort) {
        return ResponseEntity.ok(
            bookService.findAll(PageRequest.of(page, size, Sort.by(sort)))
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un livre")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche par titre, auteur ou ISBN")
    public ResponseEntity<Page<BookResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
            bookService.search(q, PageRequest.of(page, size))
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Ajouter un livre", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Modifier un livre", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BookResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un livre (admin uniquement)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
