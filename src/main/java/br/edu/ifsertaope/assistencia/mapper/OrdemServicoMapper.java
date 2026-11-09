package br.edu.ifsertaope.assistencia.mapper;

import br.edu.ifsertaope.assistencia.dto.HistoricoStatusResponse;
import br.edu.ifsertaope.assistencia.dto.OrdemServicoResponse;
import br.edu.ifsertaope.assistencia.model.Cliente;
import br.edu.ifsertaope.assistencia.model.Equipamento;
import br.edu.ifsertaope.assistencia.model.HistoricoStatus;
import br.edu.ifsertaope.assistencia.model.OrdemServico;
import br.edu.ifsertaope.assistencia.model.Tecnico;

public final class OrdemServicoMapper {

    private OrdemServicoMapper() {
    }

    public static OrdemServicoResponse paraResponse(OrdemServico ordemServico) {
        Equipamento equipamento = ordemServico.getEquipamento();
        Cliente cliente = equipamento.getCliente();
        Tecnico tecnico = ordemServico.getTecnico();
        return new OrdemServicoResponse(
                ordemServico.getId(),
                ordemServico.getStatus(),
                equipamento.getId(),
                EquipamentoMapper.descrever(equipamento),
                cliente.getId(),
                cliente.getNome(),
                tecnico == null ? null : tecnico.getId(),
                tecnico == null ? null : tecnico.getNome(),
                ordemServico.getDescricaoDefeito(),
                ordemServico.getLaudoTecnico(),
                ordemServico.getDescricaoServicos(),
                ordemServico.getValorTotal(),
                ordemServico.getDataAbertura(),
                ordemServico.getDataConclusao(),
                ordemServico.getDataEntrega());
    }

    public static HistoricoStatusResponse paraResponse(HistoricoStatus historico) {
        return new HistoricoStatusResponse(
                historico.getId(),
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                historico.getDataHora(),
                historico.getObservacao());
    }
}
