package com.bookhub.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * Service de gestion des tokens JWT.
 * Génère, valide et extrait les informations des tokens.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /** Génère un token JWT pour un utilisateur */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /** Extrait l'email (subject) du token */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** Vérifie si le token est valide pour un utilisateur donné */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
