package br.edu.ifsertaope.assistencia.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.edu.ifsertaope.assistencia.model.StatusOrdemServico;

public record OrdemServicoResponse(
        Long id,
        StatusOrdemServico status,
        Long equipamentoId,
        String equipamentoDescricao,
        Long clienteId,
        String clienteNome,
        Long tecnicoId,
        String tecnicoNome,
        String descricaoDefeito,
        String laudoTecnico,
        String descricaoServicos,
        BigDecimal valorTotal,
        LocalDateTime dataAbertura,
        LocalDateTime dataConclusao,
        LocalDateTime dataEntrega) {
}
