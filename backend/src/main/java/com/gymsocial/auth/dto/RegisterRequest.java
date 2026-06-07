package com.gymsocial.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Informe seu nome.")
    @Size(
        min = 2,
        max = 100,
        message = "O nome deve ter entre 2 e 100 caracteres."
    )
    String name,

    @NotBlank(message = "Informe seu usuário.")
    @Size(
        min = 3,
        max = 30,
        message = "O usuário deve ter entre 3 e 30 caracteres."
    )
    @Pattern(
        regexp = "^[a-zA-Z0-9._]+$",
        message = "Use somente letras, números, ponto ou sublinhado."
    )
    String username,

    @NotBlank(message = "Informe seu e-mail.")
    @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
    @Email(message = "Informe um e-mail válido.")
    String email,

    @NotBlank(message = "Informe sua senha.")
    @Size(min = 8, message = "Use pelo menos 8 caracteres.")
    @Pattern(
        regexp = ".*[a-z].*",
        message = "Inclua pelo menos uma letra minúscula."
    )
    @Pattern(
        regexp = ".*[A-Z].*",
        message = "Inclua pelo menos uma letra maiúscula."
    )
    @Pattern(
        regexp = ".*[0-9].*",
        message = "Inclua pelo menos um número."
    )
    @Pattern(
        regexp = ".*[^a-zA-Z0-9].*",
        message = "Inclua pelo menos um caractere especial."
    )
    String password
) {
}
