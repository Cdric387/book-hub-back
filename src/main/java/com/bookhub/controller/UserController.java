package com.bookhub.controller;

import com.bookhub.dto.user.ChangePasswordRequest;
import com.bookhub.dto.user.UpdateProfileRequest;
import com.bookhub.dto.user.UserResponse;
import com.bookhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des profils utilisateurs.
 *
 * GET    /api/users/me                   → mon profil
 * PUT    /api/users/me                   → modifier mon profil
 * PUT    /api/users/me/password          → changer mon mot de passe
 * DELETE /api/users/me                   → supprimer mon compte (RGPD)
 * GET    /api/users                      → tous les users (ADMIN)
 * GET    /api/users/{id}                 → un user (ADMIN)
 * PUT    /api/users/{id}/toggle-active   → activer/désactiver (ADMIN)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des profils et comptes")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // ---- Profil personnel ----

    @GetMapping("/me")
    @Operation(summary = "Consulter mon profil")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PutMapping("/me")
    @Operation(summary = "Modifier mon profil (prénom, nom, téléphone)")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Changer mon mot de passe")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Operation(summary = "Supprimer mon compte (droit à l'oubli RGPD)")
    public ResponseEntity<Void> deleteMyAccount() {
        userService.deleteMyAccount();
        return ResponseEntity.noContent().build();
    }

    // ---- Administration (ADMIN uniquement) ----

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister tous les utilisateurs")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Détail d'un utilisateur")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer / désactiver un compte utilisateur")
    public ResponseEntity<UserResponse> toggleUserActive(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserActive(id));
    }
}
