package com.bookhub.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @Email(message = "Format email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    /** Politique : 12+ car, 1 maj, 1 min, 1 chiffre, 1 spécial */
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
        message = "Mot de passe : 12+ caractères, 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial"
    )
    private String password;

    private String phone;
}
