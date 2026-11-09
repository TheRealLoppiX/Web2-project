package br.edu.ifsertaope.assistencia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AtribuicaoTecnicoRequest(

        @Schema(example = "1")
        @NotNull(message = "O campo tecnicoId é obrigatório.")
        @Positive(message = "O campo tecnicoId deve ser um número positivo.")
        Long tecnicoId) {
}
