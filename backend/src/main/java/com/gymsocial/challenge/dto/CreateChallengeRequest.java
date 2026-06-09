package com.gymsocial.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateChallengeRequest(
    @NotBlank(message = "Informe o título do desafio.")
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
    String description,

    @NotBlank(message = "Selecione o período do desafio.")
    String period,

    LocalDate endsAt,

    boolean allowMultipleCheckInsPerDay
) {
}
