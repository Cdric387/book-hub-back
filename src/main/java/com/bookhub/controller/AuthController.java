package com.bookhub.controller;

import com.bookhub.dto.auth.AuthResponse;
import com.bookhub.dto.auth.LoginRequest;
import com.bookhub.dto.auth.RegisterRequest;
import com.bookhub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour l'authentification (inscription / connexion).
 * Routes publiques : /api/auth/register et /api/auth/login
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription et connexion")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion et génération du token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
