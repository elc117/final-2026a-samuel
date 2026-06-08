package com.gymsocial.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @NotBlank(message = "Informe seu nome.")
    @Size(
        min = 2,
        max = 100,
        message = "O nome deve ter entre 2 e 100 caracteres."
    )
    String name
) {
}
