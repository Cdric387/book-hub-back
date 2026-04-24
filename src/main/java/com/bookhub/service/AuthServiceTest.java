package com.bookhub.service;

import com.bookhub.dto.auth.LoginRequest;
import com.bookhub.dto.auth.RegisterRequest;
import com.bookhub.entity.Role;
import com.bookhub.entity.User;
import com.bookhub.exception.BusinessException;
import com.bookhub.repository.UserRepository;
import com.bookhub.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du service d'authentification.
 * Couverture ciblée : inscription, connexion, cas d'erreur.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Jean");
        registerRequest.setLastName("Dupont");
        registerRequest.setEmail("jean.dupont@example.com");
        registerRequest.setPassword("MonMotDePasse1!");
        registerRequest.setPhone("0612345678");

        savedUser = User.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .password("hashed_password")
                .role(Role.USER)
                .build();
    }

    // ---- INSCRIPTION ----

    @Test
    @DisplayName("Inscription réussie → retourne token et infos utilisateur")
    void register_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("jwt_token");

        var response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getEmail()).isEqualTo("jean.dupont@example.com");
        assertThat(response.getRole()).isEqualTo("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Inscription échoue si email déjà utilisé")
    void register_emailAlreadyExists_throwsBusinessException() {
        when(userRepository.existsByEmail("jean.dupont@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Inscription encode le mot de passe avant sauvegarde")
    void register_passwordIsEncoded() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("MonMotDePasse1!")).thenReturn("bcrypt_hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertThat(u.getPassword()).isEqualTo("bcrypt_hash");
            return savedUser;
        });
        when(jwtService.generateToken(any())).thenReturn("token");

        authService.register(registerRequest);

        verify(passwordEncoder).encode("MonMotDePasse1!");
    }

    // ---- CONNEXION ----

    @Test
    @DisplayName("Connexion réussie → retourne token JWT")
    void login_success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jean.dupont@example.com");
        loginRequest.setPassword("MonMotDePasse1!");

        when(userRepository.findByEmail("jean.dupont@example.com"))
                .thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(savedUser)).thenReturn("jwt_token");

        var response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getEmail()).isEqualTo("jean.dupont@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Le rôle est bien inclus dans la réponse")
    void login_roleIncludedInResponse() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jean.dupont@example.com");
        loginRequest.setPassword("MonMotDePasse1!");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(any())).thenReturn("token");

        var response = authService.login(loginRequest);

        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getFirstName()).isEqualTo("Jean");
        assertThat(response.getLastName()).isEqualTo("Dupont");
    }
}
