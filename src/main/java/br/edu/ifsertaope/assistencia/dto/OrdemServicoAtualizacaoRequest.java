package br.edu.ifsertaope.assistencia.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

@Schema(description = "Campos não informados (nulos) mantêm o valor atual da OS")
public record OrdemServicoAtualizacaoRequest(

        @Schema(example = "Cooler obstruído e pasta térmica ressecada.")
        @Size(max = 2000, message = "O campo laudoTecnico deve ter no máximo 2000 caracteres.")
        String laudoTecnico,

        @Schema(example = "Limpeza interna e troca de pasta térmica.")
        @Size(max = 2000, message = "O campo descricaoServicos deve ter no máximo 2000 caracteres.")
        String descricaoServicos,

        @Schema(example = "150.00")
        @DecimalMin(value = "0.00", message = "O campo valorTotal não pode ser negativo.")
        @Digits(integer = 8, fraction = 2,
                message = "O campo valorTotal deve ter no máximo 8 dígitos inteiros e 2 casas decimais.")
        BigDecimal valorTotal) {
}
