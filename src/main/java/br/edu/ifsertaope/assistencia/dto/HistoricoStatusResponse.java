package br.edu.ifsertaope.assistencia.dto;

import java.time.LocalDateTime;

import br.edu.ifsertaope.assistencia.model.StatusOrdemServico;

public record HistoricoStatusResponse(
        Long id,
        StatusOrdemServico statusAnterior,
        StatusOrdemServico statusNovo,
        LocalDateTime dataHora,
        String observacao) {
}
