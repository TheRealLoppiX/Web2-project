package br.edu.ifsertaope.assistencia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrdemServicoAberturaRequest(

        @Schema(example = "1")
        @NotNull(message = "O campo equipamentoId é obrigatório.")
        @Positive(message = "O campo equipamentoId deve ser um número positivo.")
        Long equipamentoId,

        @Schema(example = "Notebook não liga e esquenta muito antes de desligar.")
        @NotBlank(message = "O campo descricaoDefeito é obrigatório.")
        @Size(max = 1000, message = "O campo descricaoDefeito deve ter no máximo 1000 caracteres.")
        String descricaoDefeito) {
}
