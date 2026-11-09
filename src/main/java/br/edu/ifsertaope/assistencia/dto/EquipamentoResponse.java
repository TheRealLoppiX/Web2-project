package br.edu.ifsertaope.assistencia.dto;

import br.edu.ifsertaope.assistencia.model.TipoEquipamento;

public record EquipamentoResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        TipoEquipamento tipo,
        String marca,
        String modelo,
        String numeroSerie,
        String especificacoes) {
}
