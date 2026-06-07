package com.gymsocial.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Informe seu e-mail.")
    @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
    @Email(message = "Informe um e-mail válido.")
    String email,

    @NotBlank(message = "Informe sua senha.")
    String password
) {
}
