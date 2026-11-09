package br.edu.ifsertaope.assistencia.dto;

import br.edu.ifsertaope.assistencia.model.TipoEquipamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EquipamentoRequest(

        @Schema(example = "1")
        @NotNull(message = "O campo clienteId é obrigatório.")
        @Positive(message = "O campo clienteId deve ser um número positivo.")
        Long clienteId,

        @Schema(example = "NOTEBOOK")
        @NotNull(message = "O campo tipo é obrigatório.")
        TipoEquipamento tipo,

        @Schema(example = "Dell")
        @NotBlank(message = "O campo marca é obrigatório.")
        @Size(max = 60, message = "O campo marca deve ter no máximo 60 caracteres.")
        String marca,

        @Schema(example = "Inspiron 15 3520")
        @NotBlank(message = "O campo modelo é obrigatório.")
        @Size(max = 80, message = "O campo modelo deve ter no máximo 80 caracteres.")
        String modelo,

        @Schema(example = "BR-5CD1234XYZ")
        @Size(max = 80, message = "O campo numeroSerie deve ter no máximo 80 caracteres.")
        String numeroSerie,

        @Schema(example = "Core i5, 8 GB RAM, SSD 256 GB; entregue com carregador")
        @Size(max = 500, message = "O campo especificacoes deve ter no máximo 500 caracteres.")
        String especificacoes) {
}
