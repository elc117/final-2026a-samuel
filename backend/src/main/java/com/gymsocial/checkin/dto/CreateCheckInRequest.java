package com.gymsocial.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCheckInRequest(
    @NotBlank(message = "Informe o título do check-in.")
    @Size(
        min = 3,
        max = 100,
        message = "O título deve ter entre 3 e 100 caracteres."
    )
    String title,

    @Size(
        max = 1000,
        message = "A descrição deve ter no máximo 1000 caracteres."
    )
    String description
) {
}
