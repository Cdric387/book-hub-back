package com.bookhub.service;

import com.bookhub.dto.user.ChangePasswordRequest;
import com.bookhub.dto.user.UpdateProfileRequest;
import com.bookhub.dto.user.UserResponse;
import com.bookhub.entity.User;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.ResourceNotFoundException;
import com.bookhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service gérant le profil utilisateur.
 * Couvre : consultation, modification, changement de mot de passe,
 * suppression de compte (droit à l'oubli RGPD) et gestion des rôles (admin).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ----------------------------------------------------------------
    // PROFIL — utilisateur connecté
    // ----------------------------------------------------------------

    /** Retourne le profil de l'utilisateur connecté */
    public UserResponse getMyProfile() {
        return toResponse(getCurrentUser());
    }

    /** Modifie prénom, nom, téléphone */
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        return toResponse(userRepository.save(user));
    }

    /** Changement de mot de passe avec vérification de l'ancien */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Mot de passe actuel incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Suppression du compte — droit à l'oubli RGPD.
     * On désactive le compte plutôt que de le supprimer physiquement
     * pour préserver l'intégrité des données historiques (emprunts, etc.)
     */
    @Transactional
    public void deleteMyAccount() {
        User user = getCurrentUser();
        user.setActive(false);
        // Anonymisation des données personnelles (RGPD)
        user.setFirstName("Utilisateur");
        user.setLastName("Supprimé");
        user.setPhone(null);
        // L'email est conservé pour éviter la réinscription avec le même email
        userRepository.save(user);
    }

    // ----------------------------------------------------------------
    // ADMIN — gestion des utilisateurs
    // ----------------------------------------------------------------

    /** Liste tous les utilisateurs — accès ADMIN uniquement */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Détail d'un utilisateur par son ID — accès ADMIN */
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + id));
    }

    /** Active ou désactive un compte — accès ADMIN */
    @Transactional
    public UserResponse toggleUserActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + id));
        user.setActive(!user.isActive());
        return toResponse(userRepository.save(user));
    }

    // ----------------------------------------------------------------
    // UTILITAIRES PRIVÉS
    // ----------------------------------------------------------------

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
