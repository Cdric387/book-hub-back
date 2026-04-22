package com.bookhub.service;

import com.bookhub.dto.auth.AuthResponse;
import com.bookhub.dto.auth.LoginRequest;
import com.bookhub.dto.auth.RegisterRequest;
import com.bookhub.entity.User;
import com.bookhub.exception.BusinessException;
import com.bookhub.repository.UserRepository;
import com.bookhub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service gérant l'inscription et la connexion des utilisateurs.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Inscription d'un nouvel utilisateur.
     * Vérifie l'unicité de l'email, hache le mot de passe, attribue le rôle USER.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return buildAuthResponse(user, token);
    }

    /**
     * Connexion d'un utilisateur existant.
     * Spring Security vérifie les credentials, on retourne un token JWT.
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        String token = jwtService.generateToken(user);
        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }
}
