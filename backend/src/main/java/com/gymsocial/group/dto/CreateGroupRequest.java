package com.gymsocial.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
    @NotBlank(message = "Informe o nome do grupo.")
    @Size(
        min = 3,
        max = 100,
        message = "O nome do grupo deve ter entre 3 e 100 caracteres."
    )
    String name,

    @Size(
        max = 2048,
        message = "A URL da imagem deve ter no máximo 2048 caracteres."
    )
    String imageUrl
) {
}
