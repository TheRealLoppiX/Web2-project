package br.edu.ifsertaope.assistencia.dto;

import br.edu.ifsertaope.assistencia.model.StatusOrdemServico;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlteracaoStatusRequest(

        @Schema(example = "ORCAMENTO_APROVADO")
        @NotNull(message = "O campo status é obrigatório.")
        StatusOrdemServico status,

        @Schema(example = "Cliente aprovou o orçamento por telefone.")
        @Size(max = 500, message = "O campo observacao deve ter no máximo 500 caracteres.")
        String observacao) {
}
