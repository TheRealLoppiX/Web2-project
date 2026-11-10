package br.edu.ifsertaope.assistencia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(example = "gerente")
        @NotBlank(message = "O campo login é obrigatório.")
        String login,

        @Schema(example = "senha-do-gerente")
        @NotBlank(message = "O campo senha é obrigatório.")
        String senha) {
}
